package com.catail.backend.signal.application;

import com.catail.backend.signal.db.SignalRepository;
import com.catail.backend.signal.domain.NewsArticle;
import com.catail.backend.signal.outbound.naver.NaverNewsItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NewsArticleCollectionServiceTest {

    @Mock private NewsArticleDeduplicator newsArticleDeduplicator;
    @Mock private NewsArticleConverter newsArticleConverter;
    @Mock private SignalRepository signalRepository;

    private NewsArticleCollectionService service() {
        return new NewsArticleCollectionService(newsArticleDeduplicator, newsArticleConverter, signalRepository);
    }

    private NewsArticleCandidate candidate(String link) {
        NaverNewsItem item = new NaverNewsItem("제목", "https://origin.example.com/1", link, "요약",
                "Mon, 10 Aug 2026 09:00:00 +0900");
        return new NewsArticleCandidate(link, item, List.of("검색어1"));
    }

    private NewsArticle article(String link) {
        return new NewsArticle("제목", "요약", "https://origin.example.com/1", link,
                OffsetDateTime.parse("2026-08-10T09:00:00+09:00"), "연합뉴스", List.of("검색어1"));
    }

    @Test
    @DisplayName("이미 저장된 Signal과 link가 겹치는 후보는 걸러내고 나머지만 변환한다")
    void collect_filtersOutExistingSignals() {
        Long catalystId = 1L;
        NewsArticleCandidate existing = candidate("https://n.news.naver.com/1");
        NewsArticleCandidate fresh = candidate("https://n.news.naver.com/2");
        Map<String, List<NaverNewsItem>> resultsByQuery = new LinkedHashMap<>();
        given(newsArticleDeduplicator.deduplicate(resultsByQuery)).willReturn(List.of(existing, fresh));
        given(signalRepository.existsByCatalystIdAndLink(catalystId, "https://n.news.naver.com/1")).willReturn(true);
        given(signalRepository.existsByCatalystIdAndLink(catalystId, "https://n.news.naver.com/2")).willReturn(false);
        given(newsArticleConverter.convert(fresh)).willReturn(article("https://n.news.naver.com/2"));

        List<NewsArticle> result = service().collect(catalystId, resultsByQuery);

        assertThat(result).extracting(NewsArticle::link).containsExactly("https://n.news.naver.com/2");
        verify(newsArticleConverter, never()).convert(existing);
    }

    @Test
    @DisplayName("기존 Signal과 겹치는 후보가 전혀 없으면 전부 변환해 반환한다")
    void collect_noExistingSignals_convertsAll() {
        Long catalystId = 1L;
        NewsArticleCandidate candidate = candidate("https://n.news.naver.com/1");
        Map<String, List<NaverNewsItem>> resultsByQuery = new LinkedHashMap<>();
        given(newsArticleDeduplicator.deduplicate(resultsByQuery)).willReturn(List.of(candidate));
        given(signalRepository.existsByCatalystIdAndLink(catalystId, "https://n.news.naver.com/1")).willReturn(false);
        given(newsArticleConverter.convert(candidate)).willReturn(article("https://n.news.naver.com/1"));

        List<NewsArticle> result = service().collect(catalystId, resultsByQuery);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("모든 후보가 이미 존재하면 빈 목록을 반환하고 변환을 호출하지 않는다")
    void collect_allExisting_returnsEmptyListWithoutConverting() {
        Long catalystId = 1L;
        NewsArticleCandidate candidate = candidate("https://n.news.naver.com/1");
        Map<String, List<NaverNewsItem>> resultsByQuery = new LinkedHashMap<>();
        given(newsArticleDeduplicator.deduplicate(resultsByQuery)).willReturn(List.of(candidate));
        given(signalRepository.existsByCatalystIdAndLink(catalystId, "https://n.news.naver.com/1")).willReturn(true);

        List<NewsArticle> result = service().collect(catalystId, resultsByQuery);

        assertThat(result).isEmpty();
        verify(newsArticleConverter, never()).convert(any());
    }
}
