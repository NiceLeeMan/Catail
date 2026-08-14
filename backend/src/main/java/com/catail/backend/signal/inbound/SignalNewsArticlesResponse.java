package com.catail.backend.signal.inbound;

import com.catail.backend.signal.outbound.naver.NaverNewsItem;

import java.util.List;
import java.util.Map;

public record SignalNewsArticlesResponse(List<QueryResult> results) {

    public record QueryResult(String query, List<NaverNewsItem> articles) {
    }

    public static SignalNewsArticlesResponse from(Map<String, List<NaverNewsItem>> resultsByQuery) {
        List<QueryResult> results = resultsByQuery.entrySet().stream()
                .map(entry -> new QueryResult(entry.getKey(), entry.getValue()))
                .toList();
        return new SignalNewsArticlesResponse(results);
    }
}
