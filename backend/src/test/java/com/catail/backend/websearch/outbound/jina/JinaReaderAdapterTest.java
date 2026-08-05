package com.catail.backend.websearch.outbound.jina;

import com.catail.backend.websearch.domain.CrawlFailureCode;
import com.catail.backend.websearch.outbound.JinaReadException;
import com.catail.backend.websearch.outbound.JinaReadResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class JinaReaderAdapterTest {

    private static final String BASE_URL = "https://r.jina.ai";

    private MockRestServiceServer mockServer;
    private JinaReaderAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .defaultHeader("Authorization", "Bearer test-key")
                .defaultHeader("Accept", "application/json");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        adapter = new JinaReaderAdapter(builder.build(), BASE_URL);
    }

    @Test
    @DisplayName("read는 baseUrl과 crawlUrl을 퍼센트 인코딩 없이 이어붙이고 필수 헤더를 담아 요청한다")
    void read_buildsUrlAndHeaders_andParsesSuccessResponse() {
        String crawlUrl = "https://example.com/article?foo=bar";
        mockServer.expect(request -> {
            assertThat(request.getURI().toString()).isEqualTo(BASE_URL + "/" + crawlUrl);
            assertThat(request.getHeaders().getFirst("Authorization")).isEqualTo("Bearer test-key");
            assertThat(request.getHeaders().getFirst("Accept")).isEqualTo("application/json");
        }).andExpect(method(GET)).andRespond(withSuccess("""
                {
                  "code": 200,
                  "status": 20000,
                  "data": {
                    "warning": null,
                    "title": "기사 제목",
                    "url": "%s",
                    "content": "정제된 본문",
                    "publishedTime": "2026-08-05T10:00:00Z",
                    "usage": { "tokens": 1520 }
                  }
                }
                """.formatted(crawlUrl), MediaType.APPLICATION_JSON));

        JinaReadResult result = adapter.read(crawlUrl);

        assertThat(result.title()).isEqualTo("기사 제목");
        assertThat(result.content()).isEqualTo("정제된 본문");
        assertThat(result.warning()).isNull();
        assertThat(result.tokens()).isEqualTo(1520);
        assertThat(result.publishedTime()).isEqualTo(LocalDateTime.of(2026, 8, 5, 10, 0, 0));
    }

    @Test
    @DisplayName("content가 빈 문자열이고 usage가 없으면 tokens는 null, content는 빈 문자열로 매핑된다")
    void read_emptyContentAndMissingUsage_mapsBlankFields() {
        mockServer.expect(method(GET)).andRespond(withSuccess("""
                { "code": 200, "status": 20000, "data": { "warning": "일부 접근 제한", "title": "", "url": "u", "content": "" } }
                """, MediaType.APPLICATION_JSON));

        JinaReadResult result = adapter.read("https://example.com/blocked");

        assertThat(result.content()).isEmpty();
        assertThat(result.warning()).isEqualTo("일부 접근 제한");
        assertThat(result.tokens()).isNull();
        assertThat(result.publishedTime()).isNull();
    }

    @Test
    @DisplayName("data 필드가 없으면 INVALID_RESPONSE 예외가 발생한다")
    void read_missingDataField_throwsInvalidResponse() {
        mockServer.expect(method(GET)).andRespond(withSuccess("""
                { "code": 200, "status": 20000 }
                """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> adapter.read("https://example.com/a"))
                .isInstanceOf(JinaReadException.class)
                .satisfies(e -> {
                    JinaReadException ex = (JinaReadException) e;
                    assertThat(ex.getFailureCode()).isEqualTo(CrawlFailureCode.INVALID_RESPONSE);
                    assertThat(ex.isRetryable()).isFalse();
                });
    }

    @Test
    @DisplayName("401 응답이면 재시도 불가능한 CLIENT_ERROR 예외가 발생한다")
    void read_unauthorized_throwsNonRetryableClientError() {
        mockServer.expect(method(GET)).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> adapter.read("https://example.com/a"))
                .isInstanceOf(JinaReadException.class)
                .satisfies(e -> {
                    JinaReadException ex = (JinaReadException) e;
                    assertThat(ex.getFailureCode()).isEqualTo(CrawlFailureCode.CLIENT_ERROR);
                    assertThat(ex.isRetryable()).isFalse();
                });
    }

    @Test
    @DisplayName("429 응답이면 재시도 가능한 RATE_LIMITED 예외가 발생한다")
    void read_tooManyRequests_throwsRetryableRateLimited() {
        mockServer.expect(method(GET)).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThatThrownBy(() -> adapter.read("https://example.com/a"))
                .isInstanceOf(JinaReadException.class)
                .satisfies(e -> {
                    JinaReadException ex = (JinaReadException) e;
                    assertThat(ex.getFailureCode()).isEqualTo(CrawlFailureCode.RATE_LIMITED);
                    assertThat(ex.isRetryable()).isTrue();
                });
    }

    @Test
    @DisplayName("5xx 응답이면 재시도 가능한 JINA_SERVER_ERROR 예외가 발생한다")
    void read_serverError_throwsRetryableServerError() {
        mockServer.expect(method(GET)).andRespond(withServerError());

        assertThatThrownBy(() -> adapter.read("https://example.com/a"))
                .isInstanceOf(JinaReadException.class)
                .satisfies(e -> {
                    JinaReadException ex = (JinaReadException) e;
                    assertThat(ex.getFailureCode()).isEqualTo(CrawlFailureCode.JINA_SERVER_ERROR);
                    assertThat(ex.isRetryable()).isTrue();
                });
    }

    @Test
    @DisplayName("네트워크 예외(IOException)면 재시도 가능한 NETWORK_ERROR 예외가 발생한다")
    void read_networkFailure_throwsRetryableNetworkError() {
        mockServer.expect(method(GET)).andRespond(request -> {
            throw new IOException("connection refused");
        });

        assertThatThrownBy(() -> adapter.read("https://example.com/a"))
                .isInstanceOf(JinaReadException.class)
                .satisfies(e -> {
                    JinaReadException ex = (JinaReadException) e;
                    assertThat(ex.getFailureCode()).isEqualTo(CrawlFailureCode.NETWORK_ERROR);
                    assertThat(ex.isRetryable()).isTrue();
                });
    }

    @Test
    @DisplayName("타임아웃 예외면 재시도 가능한 TIMEOUT 예외가 발생한다")
    void read_socketTimeout_throwsRetryableTimeout() {
        mockServer.expect(method(GET)).andRespond(request -> {
            throw new SocketTimeoutException("read timed out");
        });

        assertThatThrownBy(() -> adapter.read("https://example.com/a"))
                .isInstanceOf(JinaReadException.class)
                .satisfies(e -> {
                    JinaReadException ex = (JinaReadException) e;
                    assertThat(ex.getFailureCode()).isEqualTo(CrawlFailureCode.TIMEOUT);
                    assertThat(ex.isRetryable()).isTrue();
                });
    }
}
