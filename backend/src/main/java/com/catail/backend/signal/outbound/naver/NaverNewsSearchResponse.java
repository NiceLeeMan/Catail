package com.catail.backend.signal.outbound.naver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * <h3>네이버 뉴스 검색 API 응답 DTO</h3>
 *
 * <p>네이버 뉴스 검색 API 응답에서 서비스가 사용하는 기사 목록만 매핑한다.
 * 그 외 응답 필드는 {@link JsonIgnoreProperties} 설정을 통해 무시한다.</p>
 *
 * @param items 검색된 뉴스 기사 목록
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NaverNewsSearchResponse(List<NaverNewsItem> items) {
}
