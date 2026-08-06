package com.catail.backend.websearch.application;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public final class UrlNormalizer {

    private UrlNormalizer() {
    }

    public static String normalize(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return null;
        }
        // Outscraper 뉴스 검색 결과에는 실제 기사 URL이 아니라 "관련 검색" 등
        // Google 내부 상대경로(/goto?url=..., /search?...)가 섞여 오는 경우가 있어 걸러낸다.
        if (!isAbsoluteHttpUrl(rawUrl)) {
            return null;
        }
        String withoutFragment = rawUrl.split("#", 2)[0];

        int queryIndex = withoutFragment.indexOf('?');
        if (queryIndex < 0) {
            return withoutFragment;
        }

        String base = withoutFragment.substring(0, queryIndex);
        String query = withoutFragment.substring(queryIndex + 1);
        String filteredQuery = Arrays.stream(query.split("&"))
                .filter(param -> !param.isBlank() && !isTrackingParam(param))
                .collect(Collectors.joining("&"));

        return filteredQuery.isBlank() ? base : base + "?" + filteredQuery;
    }

    private static boolean isTrackingParam(String param) {
        String name = param.split("=", 2)[0].toLowerCase(Locale.ROOT);
        return name.startsWith("utm_");
    }

    private static boolean isAbsoluteHttpUrl(String url) {
        String lower = url.toLowerCase(Locale.ROOT);
        return lower.startsWith("http://") || lower.startsWith("https://");
    }
}
