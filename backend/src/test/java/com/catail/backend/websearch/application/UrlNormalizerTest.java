package com.catail.backend.websearch.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UrlNormalizerTest {

    @Test
    @DisplayName("fragment(#)는 제거된다")
    void normalize_removesFragment() {
        assertThat(UrlNormalizer.normalize("https://example.com/a?x=1#section"))
                .isEqualTo("https://example.com/a?x=1");
    }

    @Test
    @DisplayName("utm_ 접두사가 붙은 추적 파라미터는 제거된다")
    void normalize_removesUtmParams() {
        assertThat(UrlNormalizer.normalize(
                "https://example.com/a?utm_source=x&utm_medium=y&utm_campaign=z&keep=1"))
                .isEqualTo("https://example.com/a?keep=1");
    }

    @Test
    @DisplayName("모든 쿼리 파라미터가 추적 파라미터면 물음표까지 제거된다")
    void normalize_allTrackingParams_removesQuestionMark() {
        assertThat(UrlNormalizer.normalize("https://example.com/a?utm_source=x&utm_medium=y"))
                .isEqualTo("https://example.com/a");
    }

    @Test
    @DisplayName("추적 파라미터나 fragment가 없으면 그대로 반환한다")
    void normalize_noTrackingOrFragment_returnsUnchanged() {
        assertThat(UrlNormalizer.normalize("https://example.com/a?keep=1"))
                .isEqualTo("https://example.com/a?keep=1");
    }

    @Test
    @DisplayName("Google 클릭 추적용 상대경로(/goto?url=...)는 null을 반환한다")
    void normalize_googleGotoRelativePath_returnsNull() {
        assertThat(UrlNormalizer.normalize("/goto?url=CAESdgHuR6pNcS5SZJg7HAOyD0OR")).isNull();
    }

    @Test
    @DisplayName("Google 관련 검색 상대경로(/search?...)는 null을 반환한다")
    void normalize_googleSearchRelativePath_returnsNull() {
        assertThat(UrlNormalizer.normalize("/search?q=SK하이닉스+HBM")).isNull();
    }

    @Test
    @DisplayName("null 또는 빈 문자열은 null을 반환한다")
    void normalize_nullOrBlank_returnsNull() {
        assertThat(UrlNormalizer.normalize(null)).isNull();
        assertThat(UrlNormalizer.normalize("")).isNull();
    }
}
