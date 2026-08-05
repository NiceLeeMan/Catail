package com.catail.backend.websearch.application;

import com.catail.backend.websearch.db.*;
import com.catail.backend.websearch.outbound.OutscraperOrganicResult;
import com.catail.backend.websearch.outbound.OutscraperQueryResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Outscraper 완료 응답을 SearchResult/SearchResultQuery로 저장하고 배치를 SUCCESS로 전이시킨다.
 * 동일 배치가 재처리되어도 중복 행이 생기지 않도록 find-or-create 방식으로만 저장한다
 * (유니크 제약 위반 예외를 캐치하는 방식에 의존하지 않음).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchResultAssembler {

    private final SearchQueryRepository searchQueryRepository;
    private final SearchResultRepository searchResultRepository;
    private final SearchResultQueryRepository searchResultQueryRepository;
    private final SearchBatchRepository searchBatchRepository;
    private final SearchExecutionFinalizer searchExecutionFinalizer;

    @Transactional
    public void assembleAndComplete(Long searchExecutionId, Long batchId, List<OutscraperQueryResult> data) {
        for (OutscraperQueryResult queryResult : data) {
            assembleQueryResult(searchExecutionId, queryResult);
        }

        SearchBatch batch = searchBatchRepository.findById(batchId).orElseThrow();
        batch.markSuccess();
        searchBatchRepository.save(batch);

        searchExecutionFinalizer.finalizeIfComplete(searchExecutionId);
    }

    private void assembleQueryResult(Long searchExecutionId, OutscraperQueryResult queryResult) {
        SearchQuery query = searchQueryRepository
                .findBySearchExecutionIdAndQueryText(searchExecutionId, queryResult.query())
                .orElse(null);
        if (query == null) {
            log.warn("Outscraper 응답의 검색어가 저장된 SearchQuery와 매칭되지 않습니다: executionId={}, query={}",
                    searchExecutionId, queryResult.query());
            return;
        }

        for (OutscraperOrganicResult organic : queryResult.organicResults()) {
            linkResult(searchExecutionId, query.getId(), organic);
        }
    }

    private void linkResult(Long searchExecutionId, Long searchQueryId, OutscraperOrganicResult organic) {
        String normalizedUrl = UrlNormalizer.normalize(organic.link());
        if (normalizedUrl == null || normalizedUrl.isBlank()) {
            return;
        }

        SearchResult result = searchResultRepository
                .findBySearchExecutionIdAndCrawlUrl(searchExecutionId, normalizedUrl)
                .orElseGet(() -> searchResultRepository.save(
                        SearchResult.create(searchExecutionId, organic.title(), normalizedUrl)));

        SearchResultQueryId linkId = new SearchResultQueryId(result.getId(), searchQueryId);
        if (!searchResultQueryRepository.existsById(linkId)) {
            searchResultQueryRepository.save(SearchResultQuery.create(result.getId(), searchQueryId));
        }
    }
}
