package com.catail.backend.company.outbound.krx;

import com.catail.backend.company.domain.Market;
import com.catail.backend.company.outbound.ListedCompanyCollectionException;
import com.catail.backend.company.outbound.ListedCompanyItem;
import com.catail.backend.company.outbound.ListedCompanyPage;
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

class KrxListedCompanyAdapterTest {

    private static final String BASE_URL = "https://apis.data.go.kr/1160100/service/GetKrxListedInfoService";

    private MockRestServiceServer mockServer;
    private KrxListedCompanyAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        adapter = new KrxListedCompanyAdapter(restClient, JsonMapper.builder().build(), "test-service-key");
    }

    @Test
    @DisplayName("정상 응답이면 KOSPI 종목만 필터링하여 반환한다")
    void fetchPage_success_returnsKospiItemsOnly() {
        String body = """
                {
                  "response": {
                    "header": { "resultCode": "00", "resultMsg": "NORMAL SERVICE." },
                    "body": {
                      "totalCount": 2,
                      "items": {
                        "item": [
                          { "srtnCd": "A005930", "mrktCtg": "KOSPI", "itmsNm": "삼성전자" },
                          { "srtnCd": "A123456", "mrktCtg": "KOSDAQ", "itmsNm": "테스트코스닥" }
                        ]
                      }
                    }
                  }
                }
                """;
        mockServer.expect(method(GET)).andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        ListedCompanyPage page = adapter.fetchPage(LocalDate.of(2026, 7, 20), 1, 1000);

        assertThat(page.totalCount()).isEqualTo(2);
        assertThat(page.items()).containsExactly(new ListedCompanyItem(Market.KOSPI, "005930", "삼성전자"));
    }

    @Test
    @DisplayName("item이 1건이면 배열이 아닌 단일 객체로 와도 정상 파싱한다")
    void fetchPage_singleItemAsObject_parsesCorrectly() {
        String body = """
                {
                  "response": {
                    "header": { "resultCode": "00", "resultMsg": "NORMAL SERVICE." },
                    "body": {
                      "totalCount": 1,
                      "items": {
                        "item": { "srtnCd": "A005930", "mrktCtg": "KOSPI", "itmsNm": "삼성전자" }
                      }
                    }
                  }
                }
                """;
        mockServer.expect(method(GET)).andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        ListedCompanyPage page = adapter.fetchPage(LocalDate.of(2026, 7, 20), 1, 1);

        assertThat(page.items()).containsExactly(new ListedCompanyItem(Market.KOSPI, "005930", "삼성전자"));
    }

    @Test
    @DisplayName("srtnCd의 'A' 접두사를 제거하고 6자리 미만이면 0으로 zero-pad한다")
    void fetchPage_shortDigitsAfterPrefix_padsToSixDigits() {
        String body = """
                {
                  "response": {
                    "header": { "resultCode": "00", "resultMsg": "NORMAL SERVICE." },
                    "body": {
                      "totalCount": 1,
                      "items": {
                        "item": { "srtnCd": "A5930", "mrktCtg": "KOSPI", "itmsNm": "테스트" }
                      }
                    }
                  }
                }
                """;
        mockServer.expect(method(GET)).andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        ListedCompanyPage page = adapter.fetchPage(LocalDate.of(2026, 7, 20), 1, 1);

        assertThat(page.items()).containsExactly(new ListedCompanyItem(Market.KOSPI, "005930", "테스트"));
    }

    @Test
    @DisplayName("totalCount가 0이고 item이 없으면 빈 목록을 반환한다")
    void fetchPage_noData_returnsEmptyList() {
        String body = """
                {
                  "response": {
                    "header": { "resultCode": "00", "resultMsg": "NORMAL SERVICE." },
                    "body": { "totalCount": 0 }
                  }
                }
                """;
        mockServer.expect(method(GET)).andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        ListedCompanyPage page = adapter.fetchPage(LocalDate.of(2026, 7, 20), 1, 1);

        assertThat(page.totalCount()).isZero();
        assertThat(page.items()).isEmpty();
    }

    @Test
    @DisplayName("resultCode가 정상(00)이 아니면 ListedCompanyCollectionException이 발생한다")
    void fetchPage_errorResultCode_throwsCollectionException() {
        String body = """
                {
                  "response": {
                    "header": { "resultCode": "30", "resultMsg": "SERVICE_KEY_IS_NOT_REGISTERED_ERROR" },
                    "body": { "totalCount": 0 }
                  }
                }
                """;
        mockServer.expect(method(GET)).andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> adapter.fetchPage(LocalDate.of(2026, 7, 20), 1, 1))
                .isInstanceOf(ListedCompanyCollectionException.class);
    }

    @Test
    @DisplayName("market()은 항상 KOSPI를 반환한다")
    void market_alwaysReturnsKospi() {
        assertThat(adapter.market()).isEqualTo(Market.KOSPI);
    }
}
