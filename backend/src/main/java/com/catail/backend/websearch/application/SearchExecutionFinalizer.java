package com.catail.backend.websearch.application;

import com.catail.backend.websearch.db.SearchBatch;
import com.catail.backend.websearch.db.SearchBatchRepository;
import com.catail.backend.websearch.db.SearchExecution;
import com.catail.backend.websearch.db.SearchExecutionRepository;
import com.catail.backend.websearch.domain.SearchBatchStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 배치 하나가 종결 상태(SUCCESS/FAILED)가 될 때마다 호출되어,
 * 형제 배치가 모두 종결됐으면 SearchExecution의 최종 상태를 확정한다.
 */
@Service
@RequiredArgsConstructor
public class SearchExecutionFinalizer {

    private final SearchBatchRepository searchBatchRepository;
    private final SearchExecutionRepository searchExecutionRepository;

    @Transactional
    public void finalizeIfComplete(Long searchExecutionId) {
        List<SearchBatch> batches = searchBatchRepository.findBySearchExecutionId(searchExecutionId);
        if (batches.isEmpty()) {
            return;
        }
        boolean anyPending = batches.stream().anyMatch(b -> b.getStatus() == SearchBatchStatus.PENDING);
        if (anyPending) {
            return;
        }

        boolean allSuccess = batches.stream().allMatch(b -> b.getStatus() == SearchBatchStatus.SUCCESS);
        boolean anySuccess = batches.stream().anyMatch(b -> b.getStatus() == SearchBatchStatus.SUCCESS);

        SearchExecution execution = searchExecutionRepository.findById(searchExecutionId).orElseThrow();
        if (allSuccess) {
            execution.markSuccess();
        } else if (anySuccess) {
            execution.markPartialFailure();
        } else {
            execution.markFailure();
        }
        searchExecutionRepository.save(execution);
    }
}
