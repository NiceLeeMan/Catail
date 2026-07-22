package com.catail.backend.disclosure.outbound.opendart;

import com.catail.backend.disclosure.domain.DisclosureProvider;
import com.catail.backend.disclosure.outbound.DisclosureCollectionException;
import com.catail.backend.disclosure.outbound.DisclosureCollectionPage;
import com.catail.backend.disclosure.outbound.RawDisclosureItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenDartDisclosureAdapterTest {

    private static final String BASE_URL = "https://opendart.fss.or.kr/api";

    private MockRestServiceServer mockServer;
    private OpenDartDisclosureAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        adapter = new OpenDartDisclosureAdapter(restClient, JsonMapper.builder().build(), "test-api-key");
    }

    @Test
    @DisplayName("정상 응답이면 공시 목록을 파싱해 반환한다")
    void fetchPage_success_returnsItems() {
        String body = """
                {
                  "status": "000",
                  "message": "정상",
                  "total_count": 1,
                  "list": [
                    { "rcept_no": "20240101000123", "rcept_dt": "20240101", "report_nm": "주요사항보고서",
                      "flr_nm": "삼성전자", "rm": "유 정" }
                  ]
                }
                """;
        mockServer.expect(method(GET)).andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        DisclosureCollectionPage page = adapter.fetchPage(
                "00126380", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), 1, 100);

        assertThat(page.totalCount()).isEqualTo(1);
        assertThat(page.items()).containsExactly(new RawDisclosureItem(
                "20240101000123", LocalDate.of(2024, 1, 1), "주요사항보고서", "삼성전자", java.util.List.of("유", "정")));
    }

    @Test
    @DisplayName("status가 013(데이터없음)이면 빈 목록을 반환한다")
    void fetchPage_noDataStatus_returnsEmptyList() {
        String body = """
                { "status": "013", "message": "조회된 데이타가 없습니다." }
                """;
        mockServer.expect(method(GET)).andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        DisclosureCollectionPage page = adapter.fetchPage(
                "00126380", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), 1, 100);

        assertThat(page.totalCount()).isZero();
        assertThat(page.items()).isEmpty();
    }

    @Test
    @DisplayName("status가 정상/데이터없음이 아니면 DisclosureCollectionException이 발생한다")
    void fetchPage_errorStatus_throwsException() {
        String body = """
                { "status": "020", "message": "요청 제한을 초과하였습니다." }
                """;
        mockServer.expect(method(GET)).andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> adapter.fetchPage(
                "00126380", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), 1, 100))
                .isInstanceOf(DisclosureCollectionException.class);
    }

    @Test
    @DisplayName("rm이 없으면 remarkCodes는 빈 목록이다")
    void fetchPage_noRemark_returnsEmptyRemarkCodes() {
        String body = """
                {
                  "status": "000",
                  "total_count": 1,
                  "list": [
                    { "rcept_no": "20240101000123", "rcept_dt": "20240101", "report_nm": "보고서", "flr_nm": "삼성전자" }
                  ]
                }
                """;
        mockServer.expect(method(GET)).andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        DisclosureCollectionPage page = adapter.fetchPage(
                "00126380", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), 1, 100);

        assertThat(page.items().get(0).remarkCodes()).isEmpty();
    }

    @Test
    @DisplayName("provider()는 항상 OPEN_DART를 반환한다")
    void provider_alwaysReturnsOpenDart() {
        assertThat(adapter.provider()).isEqualTo(DisclosureProvider.OPEN_DART);
    }
}
