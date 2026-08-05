package com.catail.backend.websearch.application;

import com.catail.backend.websearch.db.SearchResult;
import com.catail.backend.websearch.db.SearchResultRepository;
import com.catail.backend.websearch.domain.CrawlStatus;
import com.catail.backend.websearch.domain.SearchExecutionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Jina 원문 수집 대상 SearchResult를 선점(claim)하고, 서버 재시작 후 남은 PROCESSING 행을 복구한다.
 * 조회와 PROCESSING 전이를 한 트랜잭션으로 묶어야 다음 스케줄 틱이 같은 행을 중복으로 집어가지 않는다.
 */
@Service
@RequiredArgsConstructor
public class CrawlCandidateSelector {

    private static final List<SearchExecutionStatus> ELIGIBLE_EXECUTION_STATUSES =
            List.of(SearchExecutionStatus.SUCCESS, SearchExecutionStatus.PARTIAL_FAILURE);

    private final SearchResultRepository searchResultRepository;

    @Transactional
    public List<SearchResult> claimNextBatch(int limit) {
        List<SearchResult> candidates = searchResultRepository.findCrawlCandidates(
                CrawlStatus.PENDING, ELIGIBLE_EXECUTION_STATUSES, PageRequest.of(0, limit));

        for (SearchResult candidate : candidates) {
            candidate.markCrawlProcessing();
        }
        return searchResultRepository.saveAll(candidates);
    }

    @Transactional
    public void recoverStaleProcessing() {
        List<SearchResult> stale = searchResultRepository.findByCrawlStatus(CrawlStatus.PROCESSING);
        for (SearchResult result : stale) {
            result.resetToPendingForRecovery();
        }
        searchResultRepository.saveAll(stale);
    }
}
