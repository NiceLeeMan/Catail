package com.catail.backend.signal.application;

import com.catail.backend.signal.outbound.naver.NaverNewsItem;

import java.util.List;

/**
 * 사이클 내 중복 제거(link 정규화 기준)까지 마친, 아직 NewsArticle로 변환되기 전의 후보.
 * normalizedLink는 중복 판단·기존 Signal 조회·최종 NewsArticle.link에 모두 동일하게 재사용한다.
 */
public record NewsArticleCandidate(
        String normalizedLink,
        NaverNewsItem item,
        List<String> sourceQueries
) {
}
