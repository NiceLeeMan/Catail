package com.catail.backend.websearch.application;

import com.catail.backend.company.domain.Market;
import com.catail.backend.global.BusinessException;
import com.catail.backend.searchplan.application.SearchPlanRequest;
import com.catail.backend.searchplan.application.SearchPlanService;
import com.catail.backend.searchplan.domain.Criterion;
import com.catail.backend.searchplan.domain.CriterionPlan;
import com.catail.backend.searchplan.domain.SearchPlanFormat;
import com.catail.backend.searchplan.domain.TargetCompanyInfo;
import com.catail.backend.websearch.application.SearchExecutionPersistenceService.BatchSubmission;
import com.catail.backend.websearch.application.SearchExecutionPersistenceService.ExecutionCreationResult;
import com.catail.backend.websearch.outbound.OutscraperSearchOptions;
import com.catail.backend.websearch.outbound.OutscraperSearchPort;
import com.catail.backend.websearch.outbound.OutscraperSubmitResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SearchExecutionServiceTest {

    @Mock
    private SearchPlanService searchPlanService;

    @Mock
    private SearchExecutionPersistenceService persistenceService;

    @Mock
    private OutscraperSearchPort outscraperSearchPort;

    @Mock
    private SearchExecutionFinalizer searchExecutionFinalizer;

    private SearchExecutionService service;

    private static final TargetCompanyInfo TARGET_COMPANY =
            new TargetCompanyInfo(1L, "삼성전자", "", "005930", Market.KOSPI, "KR");

    private void createService() {
        service = new SearchExecutionService(
                searchPlanService, persistenceService, outscraperSearchPort, searchExecutionFinalizer);
        setPagesPerQuery(service, 1);
    }

    private static void setPagesPerQuery(SearchExecutionService service, int value) {
        try {
            var field = SearchExecutionService.class.getDeclaredField("pagesPerQuery");
            field.setAccessible(true);
            field.set(service, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<String> queries(String prefix, int count) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(prefix + "_" + i);
        }
        return result;
    }

    @Test
    @DisplayName("criterion당 검색어가 15개를 초과하면 TOO_MANY_QUERIES_PER_CRITERION 예외가 발생한다")
    void create_tooManyQueriesPerCriterion_throwsException() {
        createService();
        SearchPlanFormat format = new SearchPlanFormat(TARGET_COMPANY, "HBM 사업", List.of(
                new CriterionPlan(Criterion.SUPPLIER, "관련", queries("q", 16))
        ));
        given(searchPlanService.generate(any())).willReturn(format);

        assertThatThrownBy(() -> service.create(new SearchExecutionRequest(1L, 1L, "HBM 사업")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(WebSearchErrorCode.TOO_MANY_QUERIES_PER_CRITERION);

        verify(persistenceService, never()).persistExecution(any(), any(), any(), any());
    }

    @Test
    @DisplayName("criterion 간 동일한 검색어가 있으면 DUPLICATE_QUERY_ACROSS_CRITERIA 예외가 발생한다")
    void create_duplicateQueryAcrossCriteria_throwsException() {
        createService();
        SearchPlanFormat format = new SearchPlanFormat(TARGET_COMPANY, "HBM 사업", List.of(
                new CriterionPlan(Criterion.SUPPLIER, "관련", List.of("중복 검색어")),
                new CriterionPlan(Criterion.CUSTOMER, "관련", List.of("중복 검색어"))
        ));
        given(searchPlanService.generate(any())).willReturn(format);

        assertThatThrownBy(() -> service.create(new SearchExecutionRequest(1L, 1L, "HBM 사업")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(WebSearchErrorCode.DUPLICATE_QUERY_ACROSS_CRITERIA);

        verify(persistenceService, never()).persistExecution(any(), any(), any(), any());
    }

    @Test
    @DisplayName("정상 생성 시 모든 배치를 제출하고 실행을 확정 시도한다")
    void create_success_submitsAllBatchesAndFinalizes() {
        createService();
        SearchPlanFormat format = new SearchPlanFormat(TARGET_COMPANY, "HBM 사업", List.of(
                new CriterionPlan(Criterion.SUPPLIER, "관련", List.of("검색어1", "검색어2"))
        ));
        given(searchPlanService.generate(any())).willReturn(format);

        ExecutionCreationResult creationResult = new ExecutionCreationResult(100L, List.of(
                new BatchSubmission(10L, List.of("검색어1")),
                new BatchSubmission(11L, List.of("검색어2"))
        ));
        given(persistenceService.persistExecution(eq(1L), eq(1L), eq("HBM 사업"), any())).willReturn(creationResult);
        given(outscraperSearchPort.submitBatch(any(), any(OutscraperSearchOptions.class)))
                .willReturn(new OutscraperSubmitResult("job-1", "https://loc"));

        Long executionId = service.create(new SearchExecutionRequest(1L, 1L, "HBM 사업"));

        assertThat(executionId).isEqualTo(100L);
        verify(persistenceService).markBatchSubmitted(eq(10L), any());
        verify(persistenceService).markBatchSubmitted(eq(11L), any());
        verify(persistenceService, never()).markBatchSubmitFailed(any(), any());
        verify(searchExecutionFinalizer, times(1)).finalizeIfComplete(100L);
    }

    @Test
    @DisplayName("일부 배치 제출이 실패해도 나머지 배치는 계속 제출된다")
    void create_partialSubmitFailure_continuesRemainingBatches() {
        createService();
        SearchPlanFormat format = new SearchPlanFormat(TARGET_COMPANY, "HBM 사업", List.of(
                new CriterionPlan(Criterion.SUPPLIER, "관련", List.of("검색어1", "검색어2"))
        ));
        given(searchPlanService.generate(any())).willReturn(format);

        ExecutionCreationResult creationResult = new ExecutionCreationResult(200L, List.of(
                new BatchSubmission(20L, List.of("검색어1")),
                new BatchSubmission(21L, List.of("검색어2"))
        ));
        given(persistenceService.persistExecution(eq(1L), eq(1L), eq("HBM 사업"), any())).willReturn(creationResult);
        given(outscraperSearchPort.submitBatch(eq(List.of("검색어1")), any(OutscraperSearchOptions.class)))
                .willThrow(new RuntimeException("네트워크 오류"));
        given(outscraperSearchPort.submitBatch(eq(List.of("검색어2")), any(OutscraperSearchOptions.class)))
                .willReturn(new OutscraperSubmitResult("job-2", "https://loc"));

        service.create(new SearchExecutionRequest(1L, 1L, "HBM 사업"));

        verify(persistenceService).markBatchSubmitFailed(20L, "SUBMIT_FAILED");
        verify(persistenceService).markBatchSubmitted(eq(21L), any());
        verify(searchExecutionFinalizer, times(1)).finalizeIfComplete(200L);
    }
}
