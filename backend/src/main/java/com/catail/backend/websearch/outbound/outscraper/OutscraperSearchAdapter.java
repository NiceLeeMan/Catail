package com.catail.backend.websearch.outbound.outscraper;

import com.catail.backend.websearch.outbound.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class OutscraperSearchAdapter implements OutscraperSearchPort {

    private static final Set<Integer> FATAL_STATUS_CODES = Set.of(401, 402, 422);
    private static final Set<Integer> RETRYABLE_CLIENT_STATUS_CODES = Set.of(408, 429);

    private final RestClient outscraperApiRestClient;

    public OutscraperSearchAdapter(@Qualifier("outscraperApiRestClient") RestClient outscraperApiRestClient) {
        this.outscraperApiRestClient = outscraperApiRestClient;
    }

    @Override
    public OutscraperSubmitResult submitBatch(List<String> queryTexts, OutscraperSearchOptions options) {
        try {
            JsonNode body = outscraperApiRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/google-search-news")
                            .queryParam("query", queryTexts)
                            .queryParam("pagesPerQuery", options.pagesPerQuery())
                            .queryParam("language", options.language())
                            .queryParam("region", options.region())
                            .queryParam("async", true)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);

            if (body == null) {
                throw new OutscraperSubmitException("Outscraper 배치 제출 응답이 비어 있습니다.", null);
            }
            String jobId = body.path("id").asString("");
            String resultsLocation = body.path("results_location").asString("");
            return new OutscraperSubmitResult(jobId, resultsLocation);
        } catch (RestClientException e) {
            log.error("Outscraper 배치 제출 실패", e);
            throw new OutscraperSubmitException("Outscraper 배치 제출에 실패했습니다.", e);
        }
    }

    @Override
    public OutscraperPollResult pollJob(String externalJobId) {
        try {
            JsonNode body = outscraperApiRestClient.get()
                    .uri("/requests/{id}", externalJobId)
                    .retrieve()
                    .body(JsonNode.class);

            if (body == null) {
                // 204 No Content 등 본문 없는 성공 응답은 실패로 취급한다.
                return new OutscraperPollResult(OutscraperJobStatus.FAILURE, null);
            }
            return parseResult(body);
        } catch (HttpClientErrorException e) {
            int status = e.getStatusCode().value();
            if (FATAL_STATUS_CODES.contains(status)) {
                throw new OutscraperPollException("Outscraper 인증/과금/요청 오류 (status=" + status + ")", false, null, e);
            }
            Integer retryAfterSeconds = RETRYABLE_CLIENT_STATUS_CODES.contains(status)
                    ? parseRetryAfter(e.getResponseHeaders())
                    : null;
            throw new OutscraperPollException("Outscraper 클라이언트 오류 (status=" + status + ")", true, retryAfterSeconds, e);
        } catch (HttpServerErrorException e) {
            throw new OutscraperPollException("Outscraper 서버 오류 (status=" + e.getStatusCode().value() + ")", true, null, e);
        } catch (ResourceAccessException e) {
            throw new OutscraperPollException("Outscraper 네트워크 오류", true, null, e);
        } catch (RestClientException e) {
            throw new OutscraperPollException("Outscraper 폴링 요청에 실패했습니다.", true, null, e);
        }
    }

    private OutscraperPollResult parseResult(JsonNode root) {
        String status = root.path("status").asString("");
        if ("Success".equalsIgnoreCase(status)) {
            return new OutscraperPollResult(OutscraperJobStatus.SUCCESS, parseData(root.path("data")));
        }
        if ("Pending".equalsIgnoreCase(status)) {
            return new OutscraperPollResult(OutscraperJobStatus.PENDING, null);
        }
        // "Failure" 또는 알 수 없는 상태는 모두 실패로 취급한다.
        return new OutscraperPollResult(OutscraperJobStatus.FAILURE, null);
    }

    private List<OutscraperQueryResult> parseData(JsonNode dataNode) {
        if (!dataNode.isArray()) {
            return List.of();
        }
        Map<String, List<OutscraperNewsResult>> grouped = new LinkedHashMap<>();
        for (JsonNode page : dataNode) {
            if (!page.isArray()) {
                continue;
            }
            for (JsonNode item : page) {
                String query = item.path("query").asString("");
                grouped.computeIfAbsent(query, k -> new ArrayList<>())
                        .add(new OutscraperNewsResult(
                                item.path("title").asString(""),
                                item.path("link").asString("")));
            }
        }
        List<OutscraperQueryResult> results = new ArrayList<>();
        for (Map.Entry<String, List<OutscraperNewsResult>> entry : grouped.entrySet()) {
            results.add(new OutscraperQueryResult(entry.getKey(), entry.getValue()));
        }
        return results;
    }

    private Integer parseRetryAfter(HttpHeaders headers) {
        if (headers == null) {
            return null;
        }
        String retryAfter = headers.getFirst("Retry-After");
        if (retryAfter == null || retryAfter.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(retryAfter.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
