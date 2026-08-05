package com.catail.backend.websearch.application;

import com.catail.backend.searchplan.domain.CriterionPlan;
import com.catail.backend.searchplan.domain.SearchPlanFormat;
import com.catail.backend.websearch.db.SearchBatch;
import com.catail.backend.websearch.db.SearchBatchRepository;
import com.catail.backend.websearch.db.SearchExecution;
import com.catail.backend.websearch.db.SearchExecutionRepository;
import com.catail.backend.websearch.db.SearchQuery;
import com.catail.backend.websearch.db.SearchQueryRepository;
import com.catail.backend.websearch.outbound.OutscraperSubmitResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * SearchExecutionService의 DB 쓰기 전용 협력자.
 * Outscraper 네트워크 호출과 동일 트랜잭션에 묶이지 않도록 별도 빈으로 분리했다
 * (같은 클래스 내부 self-invocation으로는 @Transactional 프록시가 적용되지 않기 때문).
 */
@Service
@RequiredArgsConstructor
public class SearchExecutionPersistenceService {

    private static final int MAX_QUERIES_PER_BATCH = 50;
    private static final String QUERY_LANGUAGE = "ko";

    private final SearchExecutionRepository searchExecutionRepository;
    private final SearchQueryRepository searchQueryRepository;
    private final SearchBatchRepository searchBatchRepository;

    @Transactional
    public ExecutionCreationResult persistExecution(Long userId, Long companyId, String analysisScope, SearchPlanFormat format) {
        SearchExecution execution = searchExecutionRepository.save(
                SearchExecution.create(userId, companyId, analysisScope));

        List<SearchQuery> allQueries = new ArrayList<>();
        for (CriterionPlan plan : format.criteria()) {
            for (String queryText : plan.queries()) {
                allQueries.add(SearchQuery.create(execution.getId(), plan.criterion(), queryText, QUERY_LANGUAGE));
            }
        }
        List<SearchQuery> savedQueries = searchQueryRepository.saveAll(allQueries);

        List<BatchSubmission> batches = new ArrayList<>();
        for (List<SearchQuery> chunk : partition(savedQueries, MAX_QUERIES_PER_BATCH)) {
            SearchBatch batch = searchBatchRepository.save(SearchBatch.create(execution.getId()));
            List<String> queryTexts = new ArrayList<>();
            for (SearchQuery query : chunk) {
                query.assignBatch(batch.getId());
                queryTexts.add(query.getQueryText());
            }
            searchQueryRepository.saveAll(chunk);
            batches.add(new BatchSubmission(batch.getId(), queryTexts));
        }

        return new ExecutionCreationResult(execution.getId(), batches);
    }

    @Transactional
    public void markBatchSubmitted(Long batchId, OutscraperSubmitResult result) {
        SearchBatch batch = searchBatchRepository.findById(batchId).orElseThrow();
        batch.markSubmitted(result.jobId(), result.resultsLocation());
        searchBatchRepository.save(batch);
    }

    @Transactional
    public void markBatchSubmitFailed(Long batchId, String reason) {
        SearchBatch batch = searchBatchRepository.findById(batchId).orElseThrow();
        batch.markFailed(reason);
        searchBatchRepository.save(batch);
    }

    private static List<List<SearchQuery>> partition(List<SearchQuery> queries, int size) {
        List<List<SearchQuery>> chunks = new ArrayList<>();
        for (int i = 0; i < queries.size(); i += size) {
            chunks.add(queries.subList(i, Math.min(i + size, queries.size())));
        }
        return chunks;
    }

    public record BatchSubmission(Long batchId, List<String> queryTexts) {
    }

    public record ExecutionCreationResult(Long executionId, List<BatchSubmission> batches) {
    }
}
