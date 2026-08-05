package com.catail.backend.websearch.db;

import com.catail.backend.searchplan.domain.Criterion;
import com.catail.backend.websearch.domain.CrawlStatus;
import com.catail.backend.websearch.domain.SearchExecutionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class WebSearchRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private SearchExecutionRepository searchExecutionRepository;

    @Autowired
    private SearchQueryRepository searchQueryRepository;

    @Autowired
    private SearchBatchRepository searchBatchRepository;

    @Autowired
    private SearchResultRepository searchResultRepository;

    @Autowired
    private SearchResultQueryRepository searchResultQueryRepository;

    private Long createExecution() {
        return searchExecutionRepository.save(SearchExecution.create(1L, 1L, "HBM 사업")).getId();
    }

    @Test
    @DisplayName("동일 실행 내 동일 query_text는 UNIQUE 제약 위반이다")
    void searchQuery_duplicateTextWithinExecution_violatesUniqueConstraint() {
        Long executionId = createExecution();
        searchQueryRepository.saveAndFlush(SearchQuery.create(executionId, Criterion.SUPPLIER, "중복 검색어", "ko"));

        assertThatThrownBy(() -> searchQueryRepository.saveAndFlush(
                SearchQuery.create(executionId, Criterion.CUSTOMER, "중복 검색어", "ko")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("다른 실행이면 동일한 query_text를 저장할 수 있다")
    void searchQuery_sameTextAcrossDifferentExecutions_allowed() {
        Long execution1 = createExecution();
        Long execution2 = createExecution();

        searchQueryRepository.saveAndFlush(SearchQuery.create(execution1, Criterion.SUPPLIER, "같은 검색어", "ko"));

        assertThat(searchQueryRepository.saveAndFlush(
                SearchQuery.create(execution2, Criterion.SUPPLIER, "같은 검색어", "ko")).getId()).isNotNull();
    }

    @Test
    @DisplayName("external_job_id는 배치 간 UNIQUE 제약이다")
    void searchBatch_duplicateExternalJobId_violatesUniqueConstraint() {
        Long executionId = createExecution();
        SearchBatch batch1 = SearchBatch.create(executionId);
        batch1.markSubmitted("job-dup", "https://loc");
        searchBatchRepository.saveAndFlush(batch1);

        SearchBatch batch2 = SearchBatch.create(executionId);
        batch2.markSubmitted("job-dup", "https://loc");

        assertThatThrownBy(() -> searchBatchRepository.saveAndFlush(batch2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("동일 실행 내 동일 crawl_url은 UNIQUE 제약 위반이다")
    void searchResult_duplicateCrawlUrlWithinExecution_violatesUniqueConstraint() {
        Long executionId = createExecution();
        searchResultRepository.saveAndFlush(SearchResult.create(executionId, "제목1", "https://example.com/a"));

        assertThatThrownBy(() -> searchResultRepository.saveAndFlush(
                SearchResult.create(executionId, "제목2", "https://example.com/a")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("SearchResultQuery는 (searchResultId, searchQueryId) 복합키로 여러 검색어를 같은 결과에 연결할 수 있다")
    void searchResultQuery_linksMultipleQueriesToSameResult() {
        Long executionId = createExecution();
        SearchQuery query1 = searchQueryRepository.save(SearchQuery.create(executionId, Criterion.SUPPLIER, "검색어1", "ko"));
        SearchQuery query2 = searchQueryRepository.save(SearchQuery.create(executionId, Criterion.CUSTOMER, "검색어2", "ko"));
        SearchResult result = searchResultRepository.save(SearchResult.create(executionId, "제목", "https://example.com/a"));

        searchResultQueryRepository.saveAndFlush(SearchResultQuery.create(result.getId(), query1.getId()));
        searchResultQueryRepository.saveAndFlush(SearchResultQuery.create(result.getId(), query2.getId()));

        assertThat(searchResultQueryRepository.existsById(new SearchResultQueryId(result.getId(), query1.getId()))).isTrue();
        assertThat(searchResultQueryRepository.existsById(new SearchResultQueryId(result.getId(), query2.getId()))).isTrue();
    }

    @Test
    @DisplayName("동일한 (searchResultId, searchQueryId) 조합을 다시 저장해도 행이 하나만 존재한다")
    void searchResultQuery_duplicateCompositeKey_staysSingleRow() {
        // @EmbeddedId는 값이 할당된(assigned) 식별자라 Spring Data save()가 매번 merge를 수행하므로
        // 예외 대신 동일 PK 행이 그대로 유지되는지(복합키 유일성)로 검증한다.
        Long executionId = createExecution();
        SearchQuery query = searchQueryRepository.save(SearchQuery.create(executionId, Criterion.SUPPLIER, "검색어", "ko"));
        SearchResult result = searchResultRepository.save(SearchResult.create(executionId, "제목", "https://example.com/a"));

        searchResultQueryRepository.saveAndFlush(SearchResultQuery.create(result.getId(), query.getId()));
        searchResultQueryRepository.saveAndFlush(SearchResultQuery.create(result.getId(), query.getId()));

        assertThat(searchResultQueryRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("SearchResult를 저장하면 crawl_status는 PENDING, retry_count는 0으로 기본 적용된다")
    void searchResult_savedWithDefaultCrawlStatusAndRetryCount() {
        Long executionId = createExecution();

        SearchResult saved = searchResultRepository.saveAndFlush(
                SearchResult.create(executionId, "제목", "https://example.com/a"));

        assertThat(saved.getCrawlStatus()).isEqualTo(CrawlStatus.PENDING);
        assertThat(saved.getRetryCount()).isZero();
    }

    @Test
    @DisplayName("findCrawlCandidates는 연관관계 없이 searchExecutionId로 SUCCESS/PARTIAL_FAILURE 실행의 PENDING 결과만 조회한다")
    void findCrawlCandidates_filtersByCrawlStatusAndExecutionStatusWithoutAssociation() {
        SearchExecution successExecution = searchExecutionRepository.save(SearchExecution.create(1L, 1L, "성공 실행"));
        successExecution.markSuccess();
        searchExecutionRepository.saveAndFlush(successExecution);
        Long pendingExecutionId = searchExecutionRepository.save(SearchExecution.create(1L, 1L, "대기 실행")).getId();

        SearchResult eligible = searchResultRepository.saveAndFlush(
                SearchResult.create(successExecution.getId(), "제목1", "https://example.com/eligible"));
        searchResultRepository.saveAndFlush(
                SearchResult.create(pendingExecutionId, "제목2", "https://example.com/not-eligible-execution"));
        SearchResult alreadyProcessing = searchResultRepository.saveAndFlush(
                SearchResult.create(successExecution.getId(), "제목3", "https://example.com/already-processing"));
        alreadyProcessing.markCrawlProcessing();
        searchResultRepository.saveAndFlush(alreadyProcessing);

        List<SearchResult> candidates = searchResultRepository.findCrawlCandidates(
                CrawlStatus.PENDING,
                List.of(SearchExecutionStatus.SUCCESS, SearchExecutionStatus.PARTIAL_FAILURE),
                PageRequest.of(0, 10));

        assertThat(candidates).extracting(SearchResult::getId).containsExactly(eligible.getId());
    }
}
