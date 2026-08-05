package com.catail.backend.websearch.inbound.read;

import com.catail.backend.websearch.db.SearchBatch;
import com.catail.backend.websearch.db.SearchExecution;
import com.catail.backend.websearch.db.SearchResult;

import java.time.LocalDateTime;
import java.util.List;

public record SearchExecutionDetailResponse(
        Long id,
        String status,
        LocalDateTime createdAt,
        LocalDateTime completedAt,
        List<BatchSummary> batches,
        List<ResultSummary> results
) {
    public static SearchExecutionDetailResponse from(
            SearchExecution execution, List<SearchBatch> batches, List<SearchResult> results) {
        return new SearchExecutionDetailResponse(
                execution.getId(),
                execution.getStatus().name(),
                execution.getCreatedAt(),
                execution.getCompletedAt(),
                batches.stream().map(BatchSummary::from).toList(),
                results.stream().map(ResultSummary::from).toList()
        );
    }

    public record BatchSummary(Long id, String status, int pollCount, String failureReason) {
        public static BatchSummary from(SearchBatch batch) {
            return new BatchSummary(batch.getId(), batch.getStatus().name(), batch.getPollCount(), batch.getFailureReason());
        }
    }

    public record ResultSummary(Long id, String title, String crawlUrl) {
        public static ResultSummary from(SearchResult result) {
            return new ResultSummary(result.getId(), result.getTitle(), result.getCrawlUrl());
        }
    }
}
