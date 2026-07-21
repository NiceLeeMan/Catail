package com.catail.backend.company.outbound.krx;

import com.catail.backend.company.domain.Market;
import com.catail.backend.company.outbound.ListedCompanyCollectionException;
import com.catail.backend.company.outbound.ListedCompanyCollectionPort;
import com.catail.backend.company.outbound.ListedCompanyItem;
import com.catail.backend.company.outbound.ListedCompanyPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class KrxListedCompanyAdapter implements ListedCompanyCollectionPort {

    private static final DateTimeFormatter BASE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String KOSPI_MARKET_CATEGORY = "KOSPI";
    private static final String SUCCESS_RESULT_CODE = "00";
    private static final int STOCK_CODE_LENGTH = 6;

    private final RestClient krxApiRestClient;
    private final ObjectMapper objectMapper;
    private final String serviceKey;

    public KrxListedCompanyAdapter(
            @Qualifier("krxApiRestClient") RestClient krxApiRestClient,
            ObjectMapper objectMapper,
            @Value("${krx.api.service-key}") String serviceKey
    ) {
        this.krxApiRestClient = krxApiRestClient;
        this.objectMapper = objectMapper;
        this.serviceKey = serviceKey;
    }

    @Override
    public Market market() {
        return Market.KOSPI;
    }

    @Override
    public ListedCompanyPage fetchPage(LocalDate baseDate, int pageNo, int numOfRows) {
        JsonNode root = requestItemInfo(baseDate, pageNo, numOfRows);

        String resultCode = root.at("/response/header/resultCode").asString("");
        if (!SUCCESS_RESULT_CODE.equals(resultCode)) {
            String resultMsg = root.at("/response/header/resultMsg").asString("");
            throw new ListedCompanyCollectionException(
                    "KRX API 응답 오류: resultCode=" + resultCode + ", resultMsg=" + resultMsg);
        }

        int totalCount = root.at("/response/body/totalCount").asInt(0);
        List<ListedCompanyItem> items = parseItems(root.at("/response/body/items/item"));

        return new ListedCompanyPage(totalCount, items);
    }

    private JsonNode requestItemInfo(LocalDate baseDate, int pageNo, int numOfRows) {
        try {
            return krxApiRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/getItemInfo")
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("resultType", "json")
                            .queryParam("basDt", baseDate.format(BASE_DATE_FORMAT))
                            .queryParam("pageNo", pageNo)
                            .queryParam("numOfRows", numOfRows)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException e) {
            throw new ListedCompanyCollectionException("KRX API 호출에 실패했습니다.", e);
        }
    }

    private List<ListedCompanyItem> parseItems(JsonNode itemNode) {
        if (itemNode.isMissingNode() || itemNode.isNull()) {
            return List.of();
        }

        List<KrxListedItem> rawItems = itemNode.isArray()
                ? Arrays.asList(objectMapper.treeToValue(itemNode, KrxListedItem[].class))
                : List.of(objectMapper.treeToValue(itemNode, KrxListedItem.class));

        return rawItems.stream()
                .filter(item -> KOSPI_MARKET_CATEGORY.equals(item.mrktCtg()))
                .map(this::toListedCompanyItem)
                .toList();
    }

    private ListedCompanyItem toListedCompanyItem(KrxListedItem item) {
        return new ListedCompanyItem(Market.KOSPI, normalizeStockCode(item.srtnCd()), item.itmsNm());
    }

    private String normalizeStockCode(String stockCode) {
        if (stockCode == null) {
            return null;
        }
        // KRX API의 srtnCd는 "A005930"처럼 알파벳 접두사가 붙어 내려오므로 제거하고 숫자만 사용한다.
        String digitsOnly = stockCode.replaceFirst("^[A-Za-z]+", "");
        if (digitsOnly.length() >= STOCK_CODE_LENGTH) {
            return digitsOnly;
        }
        return "0".repeat(STOCK_CODE_LENGTH - digitsOnly.length()) + digitsOnly;
    }
}
