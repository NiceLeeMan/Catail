package com.catail.backend.websearch.application;

import com.catail.backend.websearch.db.SearchBatch;
import com.catail.backend.websearch.db.SearchBatchRepository;
import com.catail.backend.websearch.domain.SearchBatchStatus;
import com.catail.backend.websearch.outbound.OutscraperPollException;
import com.catail.backend.websearch.outbound.OutscraperPollResult;
import com.catail.backend.websearch.outbound.OutscraperSearchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 제출된(external_job_id가 있는) PENDING 배치를 30초 간격으로 폴링해 상태를 전이시킨다.
 * 개별 배치 처리 중 예외가 나머지 배치 폴링을 막지 않도록 배치 단위로 격리한다.
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class SearchBatchPollingService {

    private static final long POLL_INTERVAL_SECONDS = 30;
    private static final int MAX_POLL_COUNT = 20;
    private static final long BATCH_TIMEOUT_MINUTES = 10;

    private final SearchBatchRepository searchBatchRepository;
    private final OutscraperSearchPort outscraperSearchPort;
    private final SearchResultAssembler searchResultAssembler;
    private final SearchExecutionFinalizer searchExecutionFinalizer;

    @Scheduled(fixedDelay = 30000)
    public void pollDueBatches() {
        List<SearchBatch> candidates =
                searchBatchRepository.findByStatusAndExternalJobIdIsNotNull(SearchBatchStatus.PENDING);
        LocalDateTime now = LocalDateTime.now();

        for (SearchBatch batch : candidates) {
            try {
                pollBatch(batch, now);
            } catch (Exception e) {
                log.error("배치 폴링 처리 중 예외 발생: batchId={}", batch.getId(), e);
            }
        }
    }

    private void pollBatch(SearchBatch batch, LocalDateTime now) {
        if (!batch.isDueForPolling(now, POLL_INTERVAL_SECONDS)) {
            return;
        }
        if (isTimedOut(batch, now)) {
            markFailed(batch, "POLLING_TIMEOUT");
            return;
        }

        OutscraperPollResult result;
        try {
            result = outscraperSearchPort.pollJob(batch.getExternalJobId());
        } catch (OutscraperPollException e) {
            if (e.isRetryable()) {
                markRetry(batch, e.getRetryAfterSeconds());
            } else {
                log.warn("배치 폴링 즉시 실패: batchId={}, reason={}", batch.getId(), e.getMessage());
                markFailed(batch, "OUTSCRAPER_FATAL_ERROR");
            }
            return;
        }

        switch (result.status()) {
            case PENDING -> markRetry(batch, null);
            case SUCCESS -> searchResultAssembler.assembleAndComplete(
                    batch.getSearchExecutionId(), batch.getId(), result.data());
            case FAILURE -> markFailed(batch, "OUTSCRAPER_FAILURE");
        }
    }

    private boolean isTimedOut(SearchBatch batch, LocalDateTime now) {
        return batch.getPollCount() >= MAX_POLL_COUNT
                || batch.getCreatedAt().plusMinutes(BATCH_TIMEOUT_MINUTES).isBefore(now);
    }

    private void markRetry(SearchBatch batch, Integer retryAfterSeconds) {
        if (retryAfterSeconds != null) {
            batch.markPolledWithRetryAfter(retryAfterSeconds);
        } else {
            batch.markPolled();
        }
        searchBatchRepository.save(batch);
    }

    private void markFailed(SearchBatch batch, String reason) {
        batch.markFailed(reason);
        searchBatchRepository.save(batch);
        searchExecutionFinalizer.finalizeIfComplete(batch.getSearchExecutionId());
    }
}
