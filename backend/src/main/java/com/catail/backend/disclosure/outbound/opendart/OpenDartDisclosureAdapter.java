package com.catail.backend.disclosure.outbound.opendart;

import com.catail.backend.disclosure.domain.DisclosureProvider;
import com.catail.backend.disclosure.outbound.DisclosureCollectionException;
import com.catail.backend.disclosure.outbound.DisclosureCollectionPage;
import com.catail.backend.disclosure.outbound.DisclosureCollectionPort;
import com.catail.backend.disclosure.outbound.RawDisclosureItem;
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
public class OpenDartDisclosureAdapter implements DisclosureCollectionPort {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String SUCCESS_STATUS = "000";
    private static final String NO_DATA_STATUS = "013";

    private final RestClient openDartApiRestClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public OpenDartDisclosureAdapter(
            @Qualifier("openDartApiRestClient") RestClient openDartApiRestClient,
            ObjectMapper objectMapper,
            @Value("${opendart.api.key}") String apiKey
    ) {
        this.openDartApiRestClient = openDartApiRestClient;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
    }

    @Override
    public DisclosureProvider provider() {
        return DisclosureProvider.OPEN_DART;
    }

    @Override
    public DisclosureCollectionPage fetchPage(
            String externalCompanyId, LocalDate beginDate, LocalDate endDate, int pageNo, int pageSize) {
        JsonNode root = requestList(externalCompanyId, beginDate, endDate, pageNo, pageSize);

        String status = root.path("status").asString("");
        if (NO_DATA_STATUS.equals(status)) {
            return new DisclosureCollectionPage(0, List.of());
        }
        if (!SUCCESS_STATUS.equals(status)) {
            String message = root.path("message").asString("");
            throw new DisclosureCollectionException("OpenDART API 응답 오류: status=" + status + ", message=" + message);
        }

        int totalCount = root.path("total_count").asInt(0);
        List<RawDisclosureItem> items = parseItems(root.path("list"));

        return new DisclosureCollectionPage(totalCount, items);
    }

    private JsonNode requestList(String corpCode, LocalDate beginDate, LocalDate endDate, int pageNo, int pageSize) {
        try {
            return openDartApiRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/list.json")
                            .queryParam("crtfc_key", apiKey)
                            .queryParam("corp_code", corpCode)
                            .queryParam("bgn_de", beginDate.format(DATE_FORMAT))
                            .queryParam("end_de", endDate.format(DATE_FORMAT))
                            .queryParam("page_no", pageNo)
                            .queryParam("page_count", pageSize)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException e) {
            throw new DisclosureCollectionException("OpenDART API 호출에 실패했습니다.", e);
        }
    }

    private List<RawDisclosureItem> parseItems(JsonNode listNode) {
        if (listNode.isMissingNode() || listNode.isNull()) {
            return List.of();
        }

        List<OpenDartDisclosureItem> rawItems = listNode.isArray()
                ? Arrays.asList(objectMapper.treeToValue(listNode, OpenDartDisclosureItem[].class))
                : List.of(objectMapper.treeToValue(listNode, OpenDartDisclosureItem.class));

        return rawItems.stream()
                .map(this::toRawDisclosureItem)
                .toList();
    }

    private RawDisclosureItem toRawDisclosureItem(OpenDartDisclosureItem item) {
        return new RawDisclosureItem(
                item.rceptNo(),
                LocalDate.parse(item.rceptDt(), DATE_FORMAT),
                item.reportNm(),
                item.flrNm(),
                parseRemarkCodes(item.rm())
        );
    }

    private List<String> parseRemarkCodes(String rm) {
        if (rm == null || rm.isBlank()) {
            return List.of();
        }
        return Arrays.stream(rm.trim().split("\\s+"))
                .filter(code -> !code.isBlank())
                .toList();
    }
}
