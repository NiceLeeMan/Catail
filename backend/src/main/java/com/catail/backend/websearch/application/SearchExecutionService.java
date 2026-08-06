package com.catail.backend.websearch.application;

import com.catail.backend.global.BusinessException;
import com.catail.backend.searchplan.application.SearchPlanRequest;
import com.catail.backend.searchplan.application.SearchPlanService;
import com.catail.backend.searchplan.domain.CriterionPlan;
import com.catail.backend.searchplan.domain.SearchPlanFormat;
import com.catail.backend.websearch.application.SearchExecutionPersistenceService.BatchSubmission;
import com.catail.backend.websearch.application.SearchExecutionPersistenceService.ExecutionCreationResult;
import com.catail.backend.websearch.outbound.OutscraperSearchOptions;
import com.catail.backend.websearch.outbound.OutscraperSearchPort;
import com.catail.backend.websearch.outbound.OutscraperSubmitResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchExecutionService {

    private static final int MAX_QUERIES_PER_CRITERION = 5;
    private static final String QUERY_LANGUAGE = "ko";
    private static final String QUERY_REGION = "KR";
    private static final String QUERY_TBS_PAST_2_YEARS = "qdr:y2";

    private final SearchPlanService searchPlanService;
    private final SearchExecutionPersistenceService persistenceService;
    private final OutscraperSearchPort outscraperSearchPort;
    private final SearchExecutionFinalizer searchExecutionFinalizer;

    @Value("${outscraper.api.pages-per-query}")
    private int pagesPerQuery;

    public Long create(SearchExecutionRequest request) {
        SearchPlanFormat format = searchPlanService.generate(
                new SearchPlanRequest(request.companyId(), request.analysisScope()));

        validate(format);

        ExecutionCreationResult creationResult = persistenceService.persistExecution(
                request.userId(), request.companyId(), request.analysisScope(), format);

        for (BatchSubmission batch : creationResult.batches()) {
            submitBatch(batch);
        }

        // 모든 배치가 제출 단계에서 이미 실패했다면(=PENDING인 채 폴링 대상이 될 배치가 없다면)
        // 폴링을 거치지 못하고 영원히 PENDING으로 남는 것을 방지하기 위해 즉시 한 번 확정 시도한다.
        searchExecutionFinalizer.finalizeIfComplete(creationResult.executionId());

        log.info("웹 검색 실행 생성 완료: executionId={}, batchCount={}",
                creationResult.executionId(), creationResult.batches().size());
        return creationResult.executionId();
    }

    private void validate(SearchPlanFormat format) {
        Set<String> seenQueries = new HashSet<>();
        for (CriterionPlan plan : format.criteria()) {
            if (plan.queries().size() > MAX_QUERIES_PER_CRITERION) {
                throw new BusinessException(WebSearchErrorCode.TOO_MANY_QUERIES_PER_CRITERION);
            }
            for (String query : plan.queries()) {
                if (!seenQueries.add(query)) {
                    throw new BusinessException(WebSearchErrorCode.DUPLICATE_QUERY_ACROSS_CRITERIA);
                }
            }
            log.info("탐색 기준별 검색어 생성 개수: criterion={}, count={}", plan.criterion(), plan.queries().size());
        }
    }

    private void submitBatch(BatchSubmission batch) {
        OutscraperSearchOptions options = new OutscraperSearchOptions(
                QUERY_LANGUAGE, QUERY_REGION, pagesPerQuery, QUERY_TBS_PAST_2_YEARS);
        try {
            OutscraperSubmitResult result = outscraperSearchPort.submitBatch(batch.queryTexts(), options);
            persistenceService.markBatchSubmitted(batch.batchId(), result);
        } catch (Exception e) {
            log.error("배치 제출 실패: batchId={}", batch.batchId(), e);
            persistenceService.markBatchSubmitFailed(batch.batchId(), "SUBMIT_FAILED");
        }
    }
}
