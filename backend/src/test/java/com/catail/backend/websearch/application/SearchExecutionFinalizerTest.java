package com.catail.backend.websearch.application;

import com.catail.backend.websearch.db.SearchBatch;
import com.catail.backend.websearch.db.SearchBatchRepository;
import com.catail.backend.websearch.db.SearchExecution;
import com.catail.backend.websearch.db.SearchExecutionRepository;
import com.catail.backend.websearch.domain.SearchExecutionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SearchExecutionFinalizerTest {

    @Mock
    private SearchBatchRepository searchBatchRepository;

    @Mock
    private SearchExecutionRepository searchExecutionRepository;

    private SearchExecutionFinalizer finalizer;

    private void createFinalizer() {
        finalizer = new SearchExecutionFinalizer(searchBatchRepository, searchExecutionRepository);
    }

    private SearchBatch batchWithStatus(Long executionId, boolean success) {
        SearchBatch batch = SearchBatch.create(executionId);
        if (success) {
            batch.markSuccess();
        } else {
            batch.markFailed("TEST");
        }
        return batch;
    }

    @Test
    @DisplayName("아직 PENDING인 형제 배치가 있으면 실행 상태를 확정하지 않는다")
    void finalizeIfComplete_anyPending_doesNothing() {
        createFinalizer();
        SearchBatch pending = SearchBatch.create(1L);
        given(searchBatchRepository.findBySearchExecutionId(1L)).willReturn(List.of(pending));

        finalizer.finalizeIfComplete(1L);

        verify(searchExecutionRepository, never()).findById(1L);
    }

    @Test
    @DisplayName("모든 배치가 SUCCESS면 실행을 SUCCESS로 확정한다")
    void finalizeIfComplete_allSuccess_marksExecutionSuccess() {
        createFinalizer();
        given(searchBatchRepository.findBySearchExecutionId(1L))
                .willReturn(List.of(batchWithStatus(1L, true), batchWithStatus(1L, true)));
        SearchExecution execution = SearchExecution.create(1L, 1L, "scope");
        given(searchExecutionRepository.findById(1L)).willReturn(Optional.of(execution));

        finalizer.finalizeIfComplete(1L);

        assertThat(execution.getStatus()).isEqualTo(SearchExecutionStatus.SUCCESS);
    }

    @Test
    @DisplayName("일부 배치만 SUCCESS면 실행을 PARTIAL_FAILURE로 확정한다")
    void finalizeIfComplete_mixedResult_marksExecutionPartialFailure() {
        createFinalizer();
        given(searchBatchRepository.findBySearchExecutionId(1L))
                .willReturn(List.of(batchWithStatus(1L, true), batchWithStatus(1L, false)));
        SearchExecution execution = SearchExecution.create(1L, 1L, "scope");
        given(searchExecutionRepository.findById(1L)).willReturn(Optional.of(execution));

        finalizer.finalizeIfComplete(1L);

        assertThat(execution.getStatus()).isEqualTo(SearchExecutionStatus.PARTIAL_FAILURE);
    }

    @Test
    @DisplayName("모든 배치가 FAILED면 실행을 FAILURE로 확정한다")
    void finalizeIfComplete_allFailed_marksExecutionFailure() {
        createFinalizer();
        given(searchBatchRepository.findBySearchExecutionId(1L))
                .willReturn(List.of(batchWithStatus(1L, false), batchWithStatus(1L, false)));
        SearchExecution execution = SearchExecution.create(1L, 1L, "scope");
        given(searchExecutionRepository.findById(1L)).willReturn(Optional.of(execution));

        finalizer.finalizeIfComplete(1L);

        assertThat(execution.getStatus()).isEqualTo(SearchExecutionStatus.FAILURE);
    }

    @Test
    @DisplayName("배치가 하나도 없으면 아무 것도 하지 않는다")
    void finalizeIfComplete_noBatches_doesNothing() {
        createFinalizer();
        given(searchBatchRepository.findBySearchExecutionId(1L)).willReturn(List.of());

        finalizer.finalizeIfComplete(1L);

        verify(searchExecutionRepository, never()).findById(1L);
    }
}
