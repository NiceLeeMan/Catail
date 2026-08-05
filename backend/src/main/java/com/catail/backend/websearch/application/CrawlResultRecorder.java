package com.catail.backend.websearch.application;

import com.catail.backend.websearch.db.SearchResult;
import com.catail.backend.websearch.db.SearchResultRepository;
import com.catail.backend.websearch.domain.CrawlFailureCode;
import com.catail.backend.websearch.outbound.JinaReadException;
import com.catail.backend.websearch.outbound.JinaReadResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Jina Reader 호출 결과를 SearchResult에 개별 저장한다.
 * 스레드마다 새 트랜잭션으로 호출되므로 SearchResult는 매번 id로 다시 조회한다.
 */
@Slf4j
@Service
public class CrawlResultRecorder {

    private final SearchResultRepository searchResultRepository;
    private final int maxRetryCount;

    public CrawlResultRecorder(
            SearchResultRepository searchResultRepository,
            @Value("${jina.api.max-retry-count}") int maxRetryCount
    ) {
        this.searchResultRepository = searchResultRepository;
        this.maxRetryCount = maxRetryCount;
    }

    @Transactional
    public void recordOutcome(Long searchResultId, JinaReadResult result) {
        SearchResult searchResult = searchResultRepository.findById(searchResultId).orElseThrow();

        boolean hasWarning = result.warning() != null && !result.warning().isBlank();
        boolean hasContent = result.content() != null && !result.content().isBlank();

        if (hasWarning && !hasContent) {
            searchResult.markCrawlFailed(CrawlFailureCode.TARGET_PAGE_ERROR, result.warning());
        } else if (!hasContent) {
            searchResult.markCrawlFailed(CrawlFailureCode.EMPTY_CONTENT, null);
        } else {
            searchResult.markCrawlSuccess(
                    result.title(), result.content(), result.publishedTime(), result.tokens(), result.warning());
        }
        searchResultRepository.save(searchResult);
    }

    @Transactional
    public void recordException(Long searchResultId, JinaReadException e) {
        SearchResult searchResult = searchResultRepository.findById(searchResultId).orElseThrow();

        if (e.isRetryable() && searchResult.getRetryCount() < maxRetryCount) {
            searchResult.markCrawlRetryPending(e.getMessage());
        } else {
            log.warn("SearchResult 크롤링 최종 실패: id={}, code={}, message={}",
                    searchResultId, e.getFailureCode(), e.getMessage());
            searchResult.markCrawlFailed(e.getFailureCode(), e.getMessage());
        }
        searchResultRepository.save(searchResult);
    }
}
