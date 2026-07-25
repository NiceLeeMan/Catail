package com.catail.backend.company.outbound.nasdaq;

import com.catail.backend.company.domain.Market;
import com.catail.backend.company.outbound.CollectionMode;
import com.catail.backend.company.outbound.ListedCompanyCollectionException;
import com.catail.backend.company.outbound.ListedCompanyItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class NasdaqListedCompanyAdapterTest {

    private static final String BASE_URL = "https://www.nasdaqtrader.com";

    private MockRestServiceServer mockServer;
    private NasdaqListedCompanyAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        adapter = new NasdaqListedCompanyAdapter(restClient);
    }

    @Test
    @DisplayName("Test Issue/ETF/NextShares 및 Market Category 필터를 통과한 보통주만 반환한다")
    void fetchSnapshot_appliesFlagAndCategoryFilters() {
        String body = """
                Symbol|Security Name|Market Category|Test Issue|Financial Status|Round Lot Size|ETF|NextShares
                AAPL|Apple Inc. - Common Stock|Q|N|N|100|N|N
                ZTEST|Test Company - Common Stock|Q|Y|N|100|N|N
                QQQ|Invesco QQQ Trust - Common Stock|G|N|N|100|Y|N
                NEXT|Nasdaq NextShares Fund - Common Stock|S|N|N|100|N|Y
                OTCX|Other Exchange Co - Common Stock|X|N|N|100|N|N
                File Creation Time: 0725202600:00|||||||
                """;
        mockServer.expect(method(GET))
                .andExpect(requestTo(BASE_URL + "/dynamic/SymDir/nasdaqlisted.txt"))
                .andRespond(withSuccess(body, MediaType.TEXT_PLAIN));

        List<ListedCompanyItem> items = adapter.fetchSnapshot();

        assertThat(items).containsExactly(new ListedCompanyItem(Market.NASDAQ, "AAPL", "Apple Inc."));
    }

    @Test
    @DisplayName("증권 유형을 판단할 수 없는 종목은 제외한다")
    void fetchSnapshot_excludesUnclassifiableSecurityType() {
        String body = """
                Symbol|Security Name|Market Category|Test Issue|Financial Status|Round Lot Size|ETF|NextShares
                AAPL|Apple Inc. - Common Stock|Q|N|N|100|N|N
                WARN|Example Corp - Warrant|Q|N|N|100|N|N
                UNK|Weird Listing Without Separator|Q|N|N|100|N|N
                """;
        mockServer.expect(method(GET)).andRespond(withSuccess(body, MediaType.TEXT_PLAIN));

        List<ListedCompanyItem> items = adapter.fetchSnapshot();

        assertThat(items).containsExactly(new ListedCompanyItem(Market.NASDAQ, "AAPL", "Apple Inc."));
    }

    @Test
    @DisplayName("심볼은 대문자로 변환하되 점·하이픈은 임의로 치환하지 않는다")
    void fetchSnapshot_uppercasesSymbolWithoutAlteringDotsOrHyphens() {
        String body = """
                Symbol|Security Name|Market Category|Test Issue|Financial Status|Round Lot Size|ETF|NextShares
                brk.b|Berkshire Hathaway Inc. - Class B Common Stock|Q|N|N|100|N|N
                """;
        mockServer.expect(method(GET)).andRespond(withSuccess(body, MediaType.TEXT_PLAIN));

        List<ListedCompanyItem> items = adapter.fetchSnapshot();

        assertThat(items)
                .containsExactly(new ListedCompanyItem(Market.NASDAQ, "BRK.B", "Berkshire Hathaway Inc."));
    }

    @Test
    @DisplayName("HTTP 호출이 실패하면 ListedCompanyCollectionException이 발생한다")
    void fetchSnapshot_httpFailure_throwsCollectionException() {
        mockServer.expect(method(GET)).andRespond(withServerError());

        assertThatThrownBy(() -> adapter.fetchSnapshot())
                .isInstanceOf(ListedCompanyCollectionException.class);
    }

    @Test
    @DisplayName("market()은 NASDAQ, mode()는 SNAPSHOT을 반환한다")
    void marketAndMode_returnNasdaqSnapshot() {
        assertThat(adapter.market()).isEqualTo(Market.NASDAQ);
        assertThat(adapter.mode()).isEqualTo(CollectionMode.SNAPSHOT);
    }
}
