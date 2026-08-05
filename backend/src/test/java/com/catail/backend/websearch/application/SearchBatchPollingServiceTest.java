package com.catail.backend.websearch.application;

import com.catail.backend.websearch.db.SearchBatch;
import com.catail.backend.websearch.db.SearchBatchRepository;
import com.catail.backend.websearch.domain.SearchBatchStatus;
import com.catail.backend.websearch.outbound.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SearchBatchPollingServiceTest {

    @Mock
    private SearchBatchRepository searchBatchRepository;

    @Mock
    private OutscraperSearchPort outscraperSearchPort;

    @Mock
    private SearchResultAssembler searchResultAssembler;

    @Mock
    private SearchExecutionFinalizer searchExecutionFinalizer;

    private SearchBatchPollingService service;

    private SearchBatch dueBatch(Long executionId, Long batchId) throws Exception {
        SearchBatch batch = SearchBatch.create(executionId);
        setField(batch, "id", batchId);
        setField(batch, "createdAt", LocalDateTime.now().minusSeconds(5));
        batch.markSubmitted("job-" + batchId, "https://loc");
        return batch;
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private void createService() {
        service = new SearchBatchPollingService(
                searchBatchRepository, outscraperSearchPort, searchResultAssembler, searchExecutionFinalizer);
    }

    @Test
    @DisplayName("최근에 폴링한 배치(30초 미경과)는 이번 tick에서 건너뛴다")
    void pollDueBatches_recentlyPolled_skipsBatch() throws Exception {
        createService();
        SearchBatch batch = dueBatch(1L, 10L);
        batch.markPolled(); // lastPolledAt = now
        given(searchBatchRepository.findByStatusAndExternalJobIdIsNotNull(SearchBatchStatus.PENDING))
                .willReturn(List.of(batch));

        service.pollDueBatches();

        verify(outscraperSearchPort, never()).pollJob(any());
    }

    @Test
    @DisplayName("poll_count가 20회 이상이면 타임아웃으로 즉시 FAILED 처리한다")
    void pollDueBatches_pollCountExceeded_marksTimeoutFailed() throws Exception {
        createService();
        SearchBatch batch = dueBatch(1L, 10L);
        setField(batch, "pollCount", 20);
        given(searchBatchRepository.findByStatusAndExternalJobIdIsNotNull(SearchBatchStatus.PENDING))
                .willReturn(List.of(batch));

        service.pollDueBatches();

        verify(outscraperSearchPort, never()).pollJob(any());
        assertThat(batch.getStatus()).isEqualTo(SearchBatchStatus.FAILED);
        assertThat(batch.getFailureReason()).isEqualTo("POLLING_TIMEOUT");
        verify(searchBatchRepository).save(batch);
        verify(searchExecutionFinalizer).finalizeIfComplete(1L);
    }

    @Test
    @DisplayName("생성 후 10분이 경과하면 타임아웃으로 즉시 FAILED 처리한다")
    void pollDueBatches_elapsedTimeExceeded_marksTimeoutFailed() throws Exception {
        createService();
        SearchBatch batch = dueBatch(1L, 10L);
        setField(batch, "createdAt", LocalDateTime.now().minusMinutes(11));
        given(searchBatchRepository.findByStatusAndExternalJobIdIsNotNull(SearchBatchStatus.PENDING))
                .willReturn(List.of(batch));

        service.pollDueBatches();

        assertThat(batch.getStatus()).isEqualTo(SearchBatchStatus.FAILED);
        assertThat(batch.getFailureReason()).isEqualTo("POLLING_TIMEOUT");
        verify(searchExecutionFinalizer).finalizeIfComplete(1L);
    }

    @Test
    @DisplayName("Pending 결과면 poll_count만 증가시키고 실행을 확정하지 않는다")
    void pollDueBatches_pendingResult_incrementsPollCountOnly() throws Exception {
        createService();
        SearchBatch batch = dueBatch(1L, 10L);
        given(searchBatchRepository.findByStatusAndExternalJobIdIsNotNull(SearchBatchStatus.PENDING))
                .willReturn(List.of(batch));
        given(outscraperSearchPort.pollJob("job-10")).willReturn(new OutscraperPollResult(OutscraperJobStatus.PENDING, null));

        service.pollDueBatches();

        assertThat(batch.getPollCount()).isEqualTo(1);
        assertThat(batch.getStatus()).isEqualTo(SearchBatchStatus.PENDING);
        verify(searchExecutionFinalizer, never()).finalizeIfComplete(any());
    }

    @Test
    @DisplayName("Success 결과면 SearchResultAssembler에 위임한다")
    void pollDueBatches_successResult_delegatesToAssembler() throws Exception {
        createService();
        SearchBatch batch = dueBatch(1L, 10L);
        List<OutscraperQueryResult> data = List.of(new OutscraperQueryResult("q", List.of()));
        given(searchBatchRepository.findByStatusAndExternalJobIdIsNotNull(SearchBatchStatus.PENDING))
                .willReturn(List.of(batch));
        given(outscraperSearchPort.pollJob("job-10")).willReturn(new OutscraperPollResult(OutscraperJobStatus.SUCCESS, data));

        service.pollDueBatches();

        verify(searchResultAssembler).assembleAndComplete(1L, 10L, data);
        verify(searchBatchRepository, never()).save(batch);
    }

    @Test
    @DisplayName("Failure 결과면 즉시 FAILED 처리 후 실행 확정을 시도한다")
    void pollDueBatches_failureResult_marksFailedAndFinalizes() throws Exception {
        createService();
        SearchBatch batch = dueBatch(1L, 10L);
        given(searchBatchRepository.findByStatusAndExternalJobIdIsNotNull(SearchBatchStatus.PENDING))
                .willReturn(List.of(batch));
        given(outscraperSearchPort.pollJob("job-10")).willReturn(new OutscraperPollResult(OutscraperJobStatus.FAILURE, null));

        service.pollDueBatches();

        assertThat(batch.getStatus()).isEqualTo(SearchBatchStatus.FAILED);
        assertThat(batch.getFailureReason()).isEqualTo("OUTSCRAPER_FAILURE");
        verify(searchExecutionFinalizer).finalizeIfComplete(1L);
    }

    @Test
    @DisplayName("재시도 가능한 예외면 Retry-After를 next_poll_at에 반영하고 실행을 확정하지 않는다")
    void pollDueBatches_retryableException_setsNextPollAt() throws Exception {
        createService();
        SearchBatch batch = dueBatch(1L, 10L);
        given(searchBatchRepository.findByStatusAndExternalJobIdIsNotNull(SearchBatchStatus.PENDING))
                .willReturn(List.of(batch));
        given(outscraperSearchPort.pollJob("job-10"))
                .willThrow(new OutscraperPollException("429", true, 45, null));

        service.pollDueBatches();

        assertThat(batch.getStatus()).isEqualTo(SearchBatchStatus.PENDING);
        assertThat(batch.getNextPollAt()).isAfter(LocalDateTime.now().plusSeconds(40));
        verify(searchExecutionFinalizer, never()).finalizeIfComplete(any());
    }

    @Test
    @DisplayName("재시도 불가능한 예외면 즉시 FAILED 처리 후 실행 확정을 시도한다")
    void pollDueBatches_nonRetryableException_marksFailedAndFinalizes() throws Exception {
        createService();
        SearchBatch batch = dueBatch(1L, 10L);
        given(searchBatchRepository.findByStatusAndExternalJobIdIsNotNull(SearchBatchStatus.PENDING))
                .willReturn(List.of(batch));
        given(outscraperSearchPort.pollJob("job-10"))
                .willThrow(new OutscraperPollException("401", false, null, null));

        service.pollDueBatches();

        assertThat(batch.getStatus()).isEqualTo(SearchBatchStatus.FAILED);
        assertThat(batch.getFailureReason()).isEqualTo("OUTSCRAPER_FATAL_ERROR");
        verify(searchExecutionFinalizer).finalizeIfComplete(1L);
    }

    @Test
    @DisplayName("한 배치에서 예기치 못한 예외가 발생해도 다른 배치는 계속 처리된다")
    void pollDueBatches_oneBatchThrowsUnexpectedException_othersStillProcessed() throws Exception {
        createService();
        SearchBatch failing = dueBatch(1L, 10L);
        SearchBatch healthy = dueBatch(2L, 11L);
        given(searchBatchRepository.findByStatusAndExternalJobIdIsNotNull(SearchBatchStatus.PENDING))
                .willReturn(List.of(failing, healthy));
        given(outscraperSearchPort.pollJob("job-10")).willThrow(new RuntimeException("예기치 못한 오류"));
        given(outscraperSearchPort.pollJob("job-11"))
                .willReturn(new OutscraperPollResult(OutscraperJobStatus.PENDING, null));

        service.pollDueBatches();

        assertThat(healthy.getPollCount()).isEqualTo(1);
    }
}
