package com.catail.backend.websearch.inbound.read;

import com.catail.backend.websearch.db.SearchBatch;
import com.catail.backend.websearch.db.SearchExecution;
import com.catail.backend.websearch.db.SearchResult;
import com.catail.backend.websearch.domain.CrawlStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record SearchExecutionDetailResponse(
        Long id,
        String status,
        LocalDateTime createdAt,
        LocalDateTime completedAt,
        List<BatchSummary> batches,
        List<ResultSummary> results,
        CrawlSummary crawlSummary
) {
    public static SearchExecutionDetailResponse from(
            SearchExecution execution, List<SearchBatch> batches, List<SearchResult> results) {
        return new SearchExecutionDetailResponse(
                execution.getId(),
                execution.getStatus().name(),
                execution.getCreatedAt(),
                execution.getCompletedAt(),
                batches.stream().map(BatchSummary::from).toList(),
                results.stream().map(ResultSummary::from).toList(),
                CrawlSummary.from(results)
        );
    }

    public record BatchSummary(Long id, String status, int pollCount, String failureReason) {
        public static BatchSummary from(SearchBatch batch) {
            return new BatchSummary(batch.getId(), batch.getStatus().name(), batch.getPollCount(), batch.getFailureReason());
        }
    }

    // content는 응답 크기가 크므로 상세조회 응답에서 제외한다.
    public record ResultSummary(Long id, String title, String crawlUrl, String crawlStatus, String failureCode) {
        public static ResultSummary from(SearchResult result) {
            return new ResultSummary(
                    result.getId(),
                    result.getTitle(),
                    result.getCrawlUrl(),
                    result.getCrawlStatus().name(),
                    result.getFailureCode() != null ? result.getFailureCode().name() : null);
        }
    }

    public record CrawlSummary(
            int pending, int processing, int success, int failed, Map<String, Long> failuresByCode) {
        public static CrawlSummary from(List<SearchResult> results) {
            Map<CrawlStatus, Long> countByStatus = results.stream()
                    .collect(Collectors.groupingBy(SearchResult::getCrawlStatus, Collectors.counting()));
            Map<String, Long> failuresByCode = results.stream()
                    .filter(r -> r.getFailureCode() != null)
                    .collect(Collectors.groupingBy(r -> r.getFailureCode().name(), Collectors.counting()));

            return new CrawlSummary(
                    countByStatus.getOrDefault(CrawlStatus.PENDING, 0L).intValue(),
                    countByStatus.getOrDefault(CrawlStatus.PROCESSING, 0L).intValue(),
                    countByStatus.getOrDefault(CrawlStatus.SUCCESS, 0L).intValue(),
                    countByStatus.getOrDefault(CrawlStatus.FAILED, 0L).intValue(),
                    failuresByCode);
        }
    }
}
