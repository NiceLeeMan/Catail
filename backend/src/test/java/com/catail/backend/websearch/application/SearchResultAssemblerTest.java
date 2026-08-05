package com.catail.backend.websearch.application;

import com.catail.backend.searchplan.domain.Criterion;
import com.catail.backend.websearch.db.*;
import com.catail.backend.websearch.domain.SearchBatchStatus;
import com.catail.backend.websearch.outbound.OutscraperOrganicResult;
import com.catail.backend.websearch.outbound.OutscraperQueryResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchResultAssemblerTest {

    @Mock
    private SearchQueryRepository searchQueryRepository;

    @Mock
    private SearchResultRepository searchResultRepository;

    @Mock
    private SearchResultQueryRepository searchResultQueryRepository;

    @Mock
    private SearchBatchRepository searchBatchRepository;

    @Mock
    private SearchExecutionFinalizer searchExecutionFinalizer;

    private SearchResultAssembler assembler;

    private void createAssembler() {
        assembler = new SearchResultAssembler(
                searchQueryRepository, searchResultRepository, searchResultQueryRepository,
                searchBatchRepository, searchExecutionFinalizer);
    }

    private static SearchQuery queryWithId(Long id) throws Exception {
        SearchQuery query = SearchQuery.create(1L, Criterion.SUPPLIER, "SK hynix HBM suppliers", "ko");
        Field field = SearchQuery.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(query, id);
        return query;
    }

    @Test
    @DisplayName("동일 URL이 여러 검색어 결과에 등장해도 SearchResult는 한 번만 생성된다")
    void assembleAndComplete_duplicateUrlAcrossQueries_createsResultOnce() throws Exception {
        createAssembler();
        SearchQuery query = queryWithId(100L);
        given(searchQueryRepository.findBySearchExecutionIdAndQueryText(1L, "SK hynix HBM suppliers"))
                .willReturn(Optional.of(query));
        given(searchResultRepository.findBySearchExecutionIdAndCrawlUrl(1L, "https://example.com/a"))
                .willReturn(Optional.empty())
                .willReturn(Optional.of(SearchResult.create(1L, "제목", "https://example.com/a")));
        given(searchResultRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(searchResultQueryRepository.existsById(any())).willReturn(false);
        given(searchBatchRepository.findById(10L)).willReturn(Optional.of(SearchBatch.create(1L)));

        List<OutscraperQueryResult> data = List.of(new OutscraperQueryResult("SK hynix HBM suppliers", List.of(
                new OutscraperOrganicResult("제목1", "https://example.com/a?utm_source=x"),
                new OutscraperOrganicResult("제목2", "https://example.com/a#frag")
        )));

        assembler.assembleAndComplete(1L, 10L, data);

        verify(searchResultRepository, times(1)).save(any());
        verify(searchExecutionFinalizer).finalizeIfComplete(1L);
    }

    @Test
    @DisplayName("이미 연결된 SearchResultQuery는 다시 저장하지 않는다")
    void assembleAndComplete_existingLink_doesNotSaveAgain() throws Exception {
        createAssembler();
        SearchQuery query = queryWithId(100L);
        given(searchQueryRepository.findBySearchExecutionIdAndQueryText(1L, "SK hynix HBM suppliers"))
                .willReturn(Optional.of(query));
        SearchResult existingResult = SearchResult.create(1L, "제목", "https://example.com/a");
        Field idField = SearchResult.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(existingResult, 500L);
        given(searchResultRepository.findBySearchExecutionIdAndCrawlUrl(1L, "https://example.com/a"))
                .willReturn(Optional.of(existingResult));
        given(searchResultQueryRepository.existsById(new SearchResultQueryId(500L, 100L))).willReturn(true);
        given(searchBatchRepository.findById(10L)).willReturn(Optional.of(SearchBatch.create(1L)));

        List<OutscraperQueryResult> data = List.of(new OutscraperQueryResult("SK hynix HBM suppliers", List.of(
                new OutscraperOrganicResult("제목", "https://example.com/a")
        )));

        assembler.assembleAndComplete(1L, 10L, data);

        verify(searchResultRepository, never()).save(any());
        verify(searchResultQueryRepository, never()).save(any());
    }

    @Test
    @DisplayName("저장된 SearchQuery와 매칭되지 않는 검색어는 건너뛰고 배치는 SUCCESS로 처리한다")
    void assembleAndComplete_unmatchedQuery_skipsButCompletesBatch() {
        createAssembler();
        given(searchQueryRepository.findBySearchExecutionIdAndQueryText(1L, "알 수 없는 검색어"))
                .willReturn(Optional.empty());
        given(searchBatchRepository.findById(10L)).willReturn(Optional.of(SearchBatch.create(1L)));

        List<OutscraperQueryResult> data = List.of(new OutscraperQueryResult("알 수 없는 검색어", List.of(
                new OutscraperOrganicResult("제목", "https://example.com/a")
        )));

        assembler.assembleAndComplete(1L, 10L, data);

        verify(searchResultRepository, never()).save(any());
        verify(searchBatchRepository).save(argThat(batch -> batch.getStatus() == SearchBatchStatus.SUCCESS));
    }
}
