package com.catail.backend.signal.outbound.naver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
/**
 * <h3>네이버 뉴스 검색 API 어댑터</h3>
 *
 * <p>검색어를 네이버 뉴스 검색 API 요청으로 변환하고,
 * 외부 응답을 {@link NaverNewsItem} 목록으로 반환한다.</p>
 *
 * <p>일시적인 통신 오류와 서버 오류에는 제한된 재시도를 적용하며,
 * 특정 검색어의 HTTP 요청 실패가 전체 검색어 수집을 중단시키지 않도록 빈 목록으로 처리한다.</p>
 */
@Slf4j
@Component
public class NaverNewsSearchAdapter {

    private static final int DISPLAY = 10;
    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_INTERVAL_MS = 2000;

    private final RestClient naverNewsApiRestClient;
    private final ObjectMapper objectMapper;

    public NaverNewsSearchAdapter(
            @Qualifier("naverNewsApiRestClient") RestClient naverNewsApiRestClient, ObjectMapper objectMapper) {
        this.naverNewsApiRestClient = naverNewsApiRestClient;
        this.objectMapper = objectMapper;
    }

    /**
     * 하나의 검색어로 네이버 뉴스 기사를 조회한다.
     *
     * <p>한 번의 요청에서 최대 {@value DISPLAY}개의 기사를 조회한다.
     * 네트워크 오류 또는 5xx 서버 오류가 발생하면 최대 {@value MAX_ATTEMPTS}회까지
     * 일정한 간격으로 재시도한다.</p>
     *
     * <p>재시도 대상이 아닌 HTTP 오류가 발생하거나 모든 재시도가 실패하면
     * 다른 검색어의 수집을 계속할 수 있도록 빈 목록을 반환한다.</p>
     *
     * @param query 네이버 뉴스 검색에 사용할 검색어
     * @return 검색된 뉴스 기사 목록 또는 요청 실패 시 빈 목록
     */
    public List<NaverNewsItem> search(String query) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                String responseBody = naverNewsApiRestClient.get()
                        .uri(uriBuilder -> uriBuilder.path("/search/v1/news")
                                .queryParam("query", query)
                                .queryParam("display", DISPLAY)
                                .build())
                        .retrieve()
                        .body(String.class);
                // NAVER API HUB(apigw.ntruss.com)는 JSON 바디를 내려주면서도 Content-Type을
                // text/plain으로 응답하는 경우가 있어, 메시지 컨버터 대신 문자열로 받아 직접 파싱한다.
                NaverNewsSearchResponse response = responseBody == null || responseBody.isBlank()
                        ? null
                        : objectMapper.readValue(responseBody, NaverNewsSearchResponse.class);
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
    /**
     * HTTP 응답 상태가 재시도 가능한 일시적 오류인지 확인한다.
     *
     * <p>현재는 5xx 서버 오류만 재시도하며,
     * 429 및 4xx 오류는 재시도하지 않는다.</p>
     *
     * @param status 네이버 뉴스 API의 HTTP 응답 상태
     * @return 재시도 가능한 상태이면 {@code true}
     */

    private boolean isRetryable(HttpStatusCode status) {
        // 500대/타임아웃만 재시도 대상. 429(일일 한도 초과)와 400/401/403/404는 재시도하지 않는다.
        return status.is5xxServerError();
    }
    /**
     * 다음 요청을 재시도하기 전에 고정된 시간만큼 현재 스레드를 대기시킨다.
     *
     * <p>대기 중 인터럽트가 발생하면 현재 스레드의 인터럽트 상태를 복원한다.</p>
     */
    private void sleepBeforeRetry() {
        try {
            Thread.sleep(RETRY_INTERVAL_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
