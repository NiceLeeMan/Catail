package com.catail.backend.websearch.application;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public final class UrlNormalizer {

    private UrlNormalizer() {
    }

    public static String normalize(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return rawUrl;
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
}
