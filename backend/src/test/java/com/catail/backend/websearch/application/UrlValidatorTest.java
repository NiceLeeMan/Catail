package com.catail.backend.websearch.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UrlValidatorTest {

    @Test
    @DisplayName("http 절대 URL은 유효하다")
    void isValid_httpAbsoluteUrl_returnsTrue() {
        assertThat(UrlValidator.isValid("http://example.com/a")).isTrue();
    }

    @Test
    @DisplayName("https 절대 URL은 유효하다")
    void isValid_httpsAbsoluteUrl_returnsTrue() {
        assertThat(UrlValidator.isValid("https://example.com/a?x=1")).isTrue();
    }

    @Test
    @DisplayName("Google 클릭 추적용 상대경로(/goto?url=...)는 무효하다")
    void isValid_googleGotoRelativePath_returnsFalse() {
        assertThat(UrlValidator.isValid("/goto?url=CAESdgHuR6pNcS5SZJg7HAOyD0OR")).isFalse();
    }

    @Test
    @DisplayName("Google 관련 검색 상대경로(/search?...)는 무효하다")
    void isValid_googleSearchRelativePath_returnsFalse() {
        assertThat(UrlValidator.isValid("/search?q=SK하이닉스+HBM")).isFalse();
    }

    @Test
    @DisplayName("scheme이 없는 문자열은 무효하다")
    void isValid_noScheme_returnsFalse() {
        assertThat(UrlValidator.isValid("example.com/a")).isFalse();
    }

    @Test
    @DisplayName("http/https가 아닌 scheme은 무효하다")
    void isValid_nonHttpScheme_returnsFalse() {
        assertThat(UrlValidator.isValid("ftp://example.com/a")).isFalse();
    }

    @Test
    @DisplayName("host가 없는 URL은 무효하다")
    void isValid_missingHost_returnsFalse() {
        assertThat(UrlValidator.isValid("mailto:a@b.com")).isFalse();
    }

    @Test
    @DisplayName("파싱이 불가능한 문자열은 무효하다")
    void isValid_unparseable_returnsFalse() {
        assertThat(UrlValidator.isValid("http://example.com/a b")).isFalse();
    }

    @Test
    @DisplayName("null 또는 빈 문자열은 무효하다")
    void isValid_nullOrBlank_returnsFalse() {
        assertThat(UrlValidator.isValid(null)).isFalse();
        assertThat(UrlValidator.isValid("")).isFalse();
        assertThat(UrlValidator.isValid("   ")).isFalse();
    }
}
