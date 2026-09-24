package com.catail.backend.signal.outbound.naver;

/**
 * <h3>네이버 뉴스 검색 결과 DTO</h3>
 *
 * <p>네이버 뉴스 검색 API가 반환하는 개별 뉴스 기사의 정보를 표현한다.</p>
 *
 * @param title        뉴스 기사 제목
 * @param originallink 언론사가 제공하는 원문 기사 URL
 * @param link         네이버 뉴스 검색 결과의 기사 URL
 * @param description  뉴스 기사 요약 내용
 * @param pubDate      뉴스 기사 발행 일시
 */
public record NaverNewsItem(
        String title,
        String originallink,
        String link,
        String description,
        String pubDate
) {
}
