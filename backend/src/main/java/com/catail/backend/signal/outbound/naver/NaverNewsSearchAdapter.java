package com.catail.backend.signal.outbound.naver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Slf4j
@Component
public class NaverNewsSearchAdapter {

    private static final int DISPLAY = 10;
    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_INTERVAL_MS = 2000;

    private final RestClient naverNewsApiRestClient;

    public NaverNewsSearchAdapter(@Qualifier("naverNewsApiRestClient") RestClient naverNewsApiRestClient) {
        this.naverNewsApiRestClient = naverNewsApiRestClient;
    }

    /**
     * 검색어 하나로 네이버 뉴스를 조회한다. 재시도 후에도 실패하면 빈 목록을 반환한다 —
     * 검색어 하나의 실패가 나머지 검색어의 병렬 수집을 막지 않도록 하기 위함.
     */
    public List<NaverNewsItem> search(String query) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                NaverNewsSearchResponse response = naverNewsApiRestClient.get()
                        .uri(uriBuilder -> uriBuilder.path("/v1/search/news.json")
                                .queryParam("query", query)
                                .queryParam("display", DISPLAY)
                                .build())
                        .retrieve()
                        .body(NaverNewsSearchResponse.class);
                return response == null || response.items() == null ? List.of() : response.items();
            } catch (RestClientResponseException e) {
                if (!isRetryable(e.getStatusCode())) {
                    log.warn("네이버 뉴스 API 재시도 대상 아닌 오류 (query={}, status={})", query, e.getStatusCode());
                    return List.of();
                }
                if (attempt == MAX_ATTEMPTS) {
                    log.warn("네이버 뉴스 API 재시도 소진 (query={}, status={})", query, e.getStatusCode());
                    return List.of();
                }
                sleepBeforeRetry();
            } catch (RestClientException e) {
                if (attempt == MAX_ATTEMPTS) {
                    log.warn("네이버 뉴스 API 재시도 소진 (query={})", query, e);
                    return List.of();
                }
                sleepBeforeRetry();
            }
        }
        return List.of();
    }

    private boolean isRetryable(HttpStatusCode status) {
        // 500대/타임아웃만 재시도 대상. 429(일일 한도 초과)와 400/401/403/404는 재시도하지 않는다.
        return status.is5xxServerError();
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(RETRY_INTERVAL_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
