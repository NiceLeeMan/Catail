package com.catail.backend.signal.application;

import com.catail.backend.signal.domain.NewsArticle;
import com.catail.backend.signal.outbound.naver.NaverNewsItem;
import com.catail.backend.signal.outbound.naver.NaverPressCrawler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class NewsArticleConverterTest {

    @Mock
    private NaverPressCrawler naverPressCrawler;

    private NewsArticleConverter converter() {
        return new NewsArticleConverter(naverPressCrawler);
    }

    private NewsArticleCandidate candidate(String title, String description) {
        NaverNewsItem item = new NaverNewsItem(
                title, "https://origin.example.com/1", "https://n.news.naver.com/1",
                description, "Mon, 10 Aug 2026 09:00:00 +0900");
        return new NewsArticleCandidate("https://n.news.naver.com/1", item, List.of("검색어1", "검색어2"));
    }

    @Test
    @DisplayName("title/description의 HTML 태그를 제거한다")
    void convert_stripsHtmlTags() {
        given(naverPressCrawler.crawl("https://n.news.naver.com/1")).willReturn(Optional.empty());

        NewsArticle result = converter().convert(
                candidate("삼성전자 <b>HBM</b> 공급 확대", "SK하이닉스와 <b>HBM</b> 계약"));

        assertThat(result.title()).isEqualTo("삼성전자 HBM 공급 확대");
        assertThat(result.description()).isEqualTo("SK하이닉스와 HBM 계약");
    }

    @Test
    @DisplayName("pubDate의 RFC822 문자열을 타임존 유지한 OffsetDateTime으로 파싱한다")
    void convert_parsesRfc822PubDateWithOffsetPreserved() {
        given(naverPressCrawler.crawl("https://n.news.naver.com/1")).willReturn(Optional.empty());

        NewsArticle result = converter().convert(candidate("제목", "요약"));

        assertThat(result.pubDate()).isEqualTo(OffsetDateTime.parse("2026-08-10T09:00:00+09:00"));
    }

    @Test
    @DisplayName("크롤러가 언론사명을 반환하면 press에 채운다")
    void convert_pressCrawlerReturnsValue_fillsPress() {
        given(naverPressCrawler.crawl("https://n.news.naver.com/1")).willReturn(Optional.of("연합뉴스"));

        NewsArticle result = converter().convert(candidate("제목", "요약"));

        assertThat(result.press()).isEqualTo("연합뉴스");
    }

    @Test
    @DisplayName("크롤러가 빈 값을 반환하면 press는 null이다")
    void convert_pressCrawlerReturnsEmpty_pressIsNull() {
        given(naverPressCrawler.crawl("https://n.news.naver.com/1")).willReturn(Optional.empty());

        NewsArticle result = converter().convert(candidate("제목", "요약"));

        assertThat(result.press()).isNull();
    }

    @Test
    @DisplayName("link는 후보의 정규화된 link를 그대로 쓰고, sourceQueries는 후보 것을 그대로 옮긴다")
    void convert_usesNormalizedLinkAndCarriesSourceQueries() {
        given(naverPressCrawler.crawl("https://n.news.naver.com/1")).willReturn(Optional.empty());

        NewsArticle result = converter().convert(candidate("제목", "요약"));

        assertThat(result.link()).isEqualTo("https://n.news.naver.com/1");
        assertThat(result.sourceQueries()).containsExactly("검색어1", "검색어2");
    }
}
