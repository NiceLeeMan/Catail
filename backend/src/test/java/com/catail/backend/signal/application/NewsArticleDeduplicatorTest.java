package com.catail.backend.signal.application;

import com.catail.backend.signal.outbound.naver.NaverNewsItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class NewsArticleDeduplicatorTest {

    private final NewsArticleDeduplicator deduplicator = new NewsArticleDeduplicator();

    private NaverNewsItem item(String link) {
        return new NaverNewsItem("제목", "https://origin.example.com/1", link, "요약",
                "Mon, 10 Aug 2026 09:00:00 +0900");
    }

    @Test
    @DisplayName("서로 다른 검색어가 반환한 동일 link는 하나의 후보로 합쳐지고 sourceQueries에 검색어가 모두 기록된다")
    void deduplicate_sameLinkFromDifferentQueries_mergesIntoOneCandidate() {
        Map<String, List<NaverNewsItem>> resultsByQuery = new LinkedHashMap<>();
        resultsByQuery.put("검색어1", List.of(item("https://n.news.naver.com/1")));
        resultsByQuery.put("검색어2", List.of(item("https://n.news.naver.com/1")));

        List<NewsArticleCandidate> result = deduplicator.deduplicate(resultsByQuery);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).sourceQueries()).containsExactly("검색어1", "검색어2");
    }

    @Test
    @DisplayName("link가 다르면 별개 후보로 유지된다")
    void deduplicate_differentLinks_keepsSeparateCandidates() {
        Map<String, List<NaverNewsItem>> resultsByQuery = new LinkedHashMap<>();
        resultsByQuery.put("검색어1", List.of(item("https://n.news.naver.com/1"), item("https://n.news.naver.com/2")));

        List<NewsArticleCandidate> result = deduplicator.deduplicate(resultsByQuery);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("http/https, trailing slash 차이만 있는 link는 동일 link로 취급한다")
    void deduplicate_schemeAndTrailingSlashVariants_treatedAsSameLink() {
        Map<String, List<NaverNewsItem>> resultsByQuery = new LinkedHashMap<>();
        resultsByQuery.put("검색어1", List.of(item("http://n.news.naver.com/1/")));
        resultsByQuery.put("검색어2", List.of(item("https://n.news.naver.com/1")));

        List<NewsArticleCandidate> result = deduplicator.deduplicate(resultsByQuery);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).normalizedLink()).isEqualTo("https://n.news.naver.com/1");
    }

    @Test
    @DisplayName("같은 검색어 내에서 동일 link가 중복 반환돼도 sourceQueries에는 한 번만 기록된다")
    void deduplicate_sameQueryDuplicateLink_recordsQueryOnce() {
        Map<String, List<NaverNewsItem>> resultsByQuery = new LinkedHashMap<>();
        resultsByQuery.put("검색어1", List.of(item("https://n.news.naver.com/1"), item("https://n.news.naver.com/1")));

        List<NewsArticleCandidate> result = deduplicator.deduplicate(resultsByQuery);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).sourceQueries()).containsExactly("검색어1");
    }
}
