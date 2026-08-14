package com.catail.backend.signal.application;

import com.catail.backend.signal.outbound.naver.NaverNewsItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class NewsArticleDeduplicator {

    public List<NewsArticleCandidate> deduplicate(Map<String, List<NaverNewsItem>> resultsByQuery) {
        Map<String, NaverNewsItem> itemByLink = new LinkedHashMap<>();
        Map<String, List<String>> queriesByLink = new LinkedHashMap<>();

        resultsByQuery.forEach((query, items) -> items.forEach(item -> {
            String normalizedLink = normalize(item.link());
            itemByLink.putIfAbsent(normalizedLink, item);
            List<String> queries = queriesByLink.computeIfAbsent(normalizedLink, key -> new ArrayList<>());
            if (!queries.contains(query)) {
                queries.add(query);
            }
        }));

        return itemByLink.entrySet().stream()
                .map(entry -> new NewsArticleCandidate(
                        entry.getKey(), entry.getValue(), List.copyOf(queriesByLink.get(entry.getKey()))))
                .toList();
    }

    private String normalize(String link) {
        String normalized = link.trim();
        if (normalized.startsWith("http://")) {
            normalized = "https://" + normalized.substring("http://".length());
        }
        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
