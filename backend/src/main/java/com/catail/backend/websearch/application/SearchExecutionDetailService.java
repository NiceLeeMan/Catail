package com.catail.backend.websearch.application;

import com.catail.backend.global.BusinessException;
import com.catail.backend.websearch.db.SearchBatchRepository;
import com.catail.backend.websearch.db.SearchExecution;
import com.catail.backend.websearch.db.SearchExecutionRepository;
import com.catail.backend.websearch.db.SearchResultRepository;
import com.catail.backend.websearch.inbound.read.SearchExecutionDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchExecutionDetailService {

    private final SearchExecutionRepository searchExecutionRepository;
    private final SearchBatchRepository searchBatchRepository;
    private final SearchResultRepository searchResultRepository;

    @Transactional(readOnly = true)
    public SearchExecutionDetailResponse getDetail(Long executionId) {
        SearchExecution execution = searchExecutionRepository.findById(executionId)
                .orElseThrow(() -> new BusinessException(WebSearchErrorCode.SEARCH_EXECUTION_NOT_FOUND));

        return SearchExecutionDetailResponse.from(
                execution,
                searchBatchRepository.findBySearchExecutionId(executionId),
                searchResultRepository.findBySearchExecutionId(executionId)
        );
    }
}
