package com.catail.backend.websearch.application;

import com.catail.backend.websearch.db.SearchResult;
import com.catail.backend.websearch.db.SearchResultRepository;
import com.catail.backend.websearch.domain.CrawlStatus;
import com.catail.backend.websearch.domain.SearchExecutionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CrawlCandidateSelectorTest {

    @Mock
    private SearchResultRepository searchResultRepository;

    private CrawlCandidateSelector selector;

    private void createSelector() {
        selector = new CrawlCandidateSelector(searchResultRepository);
    }

    @Test
    @DisplayName("claimNextBatch는 조회한 후보를 곧바로 PROCESSING으로 전이시키고 저장한다")
    void claimNextBatch_marksCandidatesProcessing() {
        createSelector();
        SearchResult candidate = SearchResult.create(1L, "제목", "https://example.com/a");
        given(searchResultRepository.findCrawlCandidates(
                eq(CrawlStatus.PENDING),
                eq(List.of(SearchExecutionStatus.SUCCESS, SearchExecutionStatus.PARTIAL_FAILURE)),
                eq(PageRequest.of(0, 5))))
                .willReturn(List.of(candidate));
        given(searchResultRepository.saveAll(List.of(candidate))).willReturn(List.of(candidate));

        List<SearchResult> claimed = selector.claimNextBatch(5);

        assertThat(claimed).containsExactly(candidate);
        assertThat(candidate.getCrawlStatus()).isEqualTo(CrawlStatus.PROCESSING);
        verify(searchResultRepository).saveAll(List.of(candidate));
    }

    @Test
    @DisplayName("recoverStaleProcessing은 PROCESSING으로 남은 행을 PENDING으로 되돌린다")
    void recoverStaleProcessing_resetsProcessingToPending() {
        createSelector();
        SearchResult stale = SearchResult.create(1L, "제목", "https://example.com/a");
        stale.markCrawlProcessing();
        given(searchResultRepository.findByCrawlStatus(CrawlStatus.PROCESSING)).willReturn(List.of(stale));
        given(searchResultRepository.saveAll(List.of(stale))).willReturn(List.of(stale));

        selector.recoverStaleProcessing();

        assertThat(stale.getCrawlStatus()).isEqualTo(CrawlStatus.PENDING);
        assertThat(stale.getRetryCount()).isZero();
        verify(searchResultRepository).saveAll(List.of(stale));
    }
}
