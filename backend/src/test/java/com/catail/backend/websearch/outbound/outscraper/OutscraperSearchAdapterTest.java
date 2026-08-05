package com.catail.backend.websearch.outbound.outscraper;

import com.catail.backend.websearch.outbound.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class OutscraperSearchAdapterTest {

    private static final String BASE_URL = "https://api.outscraper.com";

    private MockRestServiceServer mockServer;
    private OutscraperSearchAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        mockServer = MockRestServiceServer.bindTo(builder).build();
        adapter = new OutscraperSearchAdapter(builder.build());
    }

    @Test
    @DisplayName("submitBatch는 검색어를 query 파라미터로 반복해서 담아 요청한다")
    void submitBatch_repeatsQueryParamForEachText() {
        mockServer.expect(request -> {
            String uri = request.getURI().toString();
            assertThat(uri).contains("/google-search");
            assertThat(uri).contains("query=supplierQuery1");
            assertThat(uri).contains("query=supplierQuery2");
            assertThat(uri).contains("pagesPerQuery=1");
            assertThat(uri).contains("language=ko");
            assertThat(uri).contains("region=KR");
            assertThat(uri).contains("async=true");
        }).andExpect(method(GET)).andRespond(withSuccess("""
                { "id": "job-123", "status": "Pending", "results_location": "https://api.outscraper.cloud/requests/job-123" }
                """, MediaType.APPLICATION_JSON));

        OutscraperSubmitResult result = adapter.submitBatch(
                List.of("supplierQuery1", "supplierQuery2"),
                new OutscraperSearchOptions("ko", "KR", 1));

        assertThat(result.jobId()).isEqualTo("job-123");
        assertThat(result.resultsLocation()).isEqualTo("https://api.outscraper.cloud/requests/job-123");
    }

    @Test
    @DisplayName("submitBatch 호출이 실패하면 OutscraperSubmitException이 발생한다")
    void submitBatch_httpError_throwsSubmitException() {
        mockServer.expect(method(GET)).andRespond(withServerError());

        assertThatThrownBy(() -> adapter.submitBatch(List.of("q"), new OutscraperSearchOptions("ko", "KR", 1)))
                .isInstanceOf(OutscraperSubmitException.class);
    }

    @Test
    @DisplayName("Pending 상태 응답이면 OutscraperJobStatus.PENDING을 반환한다")
    void pollJob_pending_returnsPendingStatus() {
        mockServer.expect(method(GET)).andRespond(withSuccess("""
                { "id": "job-123", "status": "Pending" }
                """, MediaType.APPLICATION_JSON));

        OutscraperPollResult result = adapter.pollJob("job-123");

        assertThat(result.status()).isEqualTo(OutscraperJobStatus.PENDING);
    }

    @Test
    @DisplayName("Success 상태 응답이면 query/title/link를 파싱해 반환한다")
    void pollJob_success_parsesQueryAndOrganicResults() {
        mockServer.expect(method(GET)).andRespond(withSuccess("""
                {
                  "id": "job-123",
                  "status": "Success",
                  "data": [
                    {
                      "query": "SK hynix HBM suppliers",
                      "organic_results": [
                        { "title": "제목1", "link": "https://example.com/a?utm_source=x#frag" }
                      ]
                    },
                    {
                      "query": "SK hynix HBM customers",
                      "organic_results": []
                    }
                  ]
                }
                """, MediaType.APPLICATION_JSON));

        OutscraperPollResult result = adapter.pollJob("job-123");

        assertThat(result.status()).isEqualTo(OutscraperJobStatus.SUCCESS);
        assertThat(result.data()).hasSize(2);
        assertThat(result.data().get(0).query()).isEqualTo("SK hynix HBM suppliers");
        assertThat(result.data().get(0).organicResults()).containsExactly(
                new OutscraperOrganicResult("제목1", "https://example.com/a?utm_source=x#frag"));
        assertThat(result.data().get(1).organicResults()).isEmpty();
    }

    @Test
    @DisplayName("Failure 상태 응답이면 FAILURE를 반환한다")
    void pollJob_failureStatus_returnsFailure() {
        mockServer.expect(method(GET)).andRespond(withSuccess("""
                { "id": "job-123", "status": "Failure" }
                """, MediaType.APPLICATION_JSON));

        OutscraperPollResult result = adapter.pollJob("job-123");

        assertThat(result.status()).isEqualTo(OutscraperJobStatus.FAILURE);
    }

    @Test
    @DisplayName("204 No Content 응답이면 FAILURE를 반환한다")
    void pollJob_noContent_returnsFailure() {
        mockServer.expect(method(GET)).andRespond(withStatus(HttpStatus.NO_CONTENT));

        OutscraperPollResult result = adapter.pollJob("job-123");

        assertThat(result.status()).isEqualTo(OutscraperJobStatus.FAILURE);
    }

    @Test
    @DisplayName("401 응답이면 재시도 불가능한 예외가 발생한다")
    void pollJob_unauthorized_throwsNonRetryableException() {
        mockServer.expect(method(GET)).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> adapter.pollJob("job-123"))
                .isInstanceOf(OutscraperPollException.class)
                .satisfies(e -> assertThat(((OutscraperPollException) e).isRetryable()).isFalse());
    }

    @Test
    @DisplayName("422 응답이면 재시도 불가능한 예외가 발생한다")
    void pollJob_unprocessable_throwsNonRetryableException() {
        mockServer.expect(method(GET)).andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY));

        assertThatThrownBy(() -> adapter.pollJob("job-123"))
                .isInstanceOf(OutscraperPollException.class)
                .satisfies(e -> assertThat(((OutscraperPollException) e).isRetryable()).isFalse());
    }

    @Test
    @DisplayName("429 응답이면 Retry-After 값을 담아 재시도 가능한 예외가 발생한다")
    void pollJob_tooManyRequests_throwsRetryableExceptionWithRetryAfter() {
        mockServer.expect(method(GET)).andRespond(
                withStatus(HttpStatus.TOO_MANY_REQUESTS).header("Retry-After", "45"));

        assertThatThrownBy(() -> adapter.pollJob("job-123"))
                .isInstanceOf(OutscraperPollException.class)
                .satisfies(e -> {
                    OutscraperPollException ex = (OutscraperPollException) e;
                    assertThat(ex.isRetryable()).isTrue();
                    assertThat(ex.getRetryAfterSeconds()).isEqualTo(45);
                });
    }

    @Test
    @DisplayName("5xx 응답이면 재시도 가능한 예외가 발생한다")
    void pollJob_serverError_throwsRetryableException() {
        mockServer.expect(method(GET)).andRespond(withServerError());

        assertThatThrownBy(() -> adapter.pollJob("job-123"))
                .isInstanceOf(OutscraperPollException.class)
                .satisfies(e -> assertThat(((OutscraperPollException) e).isRetryable()).isTrue());
    }
}
