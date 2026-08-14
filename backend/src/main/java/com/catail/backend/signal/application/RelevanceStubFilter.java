package com.catail.backend.signal.application;

import com.catail.backend.signal.domain.NewsArticle;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 관련성 검사 스텁 — 현재는 판단 기준이 언어화되지 않아 필터링 없이 전부 통과시킨다.
 * 실제 수집 데이터를 확보한 뒤 판단 기준을 언어화해 LLM 프롬프트로 교체될 자리다.
 */
@Component
public class RelevanceStubFilter {

    public List<NewsArticle> filter(List<NewsArticle> articles) {
        return articles;
    }
}
