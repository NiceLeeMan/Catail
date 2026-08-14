package com.catail.backend.signal.application;

import com.catail.backend.signal.domain.NewsArticle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RelevanceStubFilterTest {

    private final RelevanceStubFilter filter = new RelevanceStubFilter();

    @Test
    @DisplayName("현재는 스텁이라 입력받은 기사를 그대로 통과시킨다")
    void filter_returnsAllArticlesUnchanged() {
        NewsArticle article = new NewsArticle("제목", "요약", "https://origin.example.com/1",
                "https://n.news.naver.com/1", OffsetDateTime.parse("2026-08-10T09:00:00+09:00"),
                "연합뉴스", List.of("검색어1"));

        List<NewsArticle> result = filter.filter(List.of(article));

        assertThat(result).containsExactly(article);
    }

    @Test
    @DisplayName("빈 목록을 넣으면 빈 목록을 반환한다")
    void filter_emptyList_returnsEmptyList() {
        List<NewsArticle> result = filter.filter(List.of());

        assertThat(result).isEmpty();
    }
}
