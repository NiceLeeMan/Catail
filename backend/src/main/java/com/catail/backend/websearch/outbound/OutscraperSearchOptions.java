package com.catail.backend.websearch.outbound;

public record OutscraperSearchOptions(
        String language,
        String region,
        int pagesPerQuery,
        String tbs
) {
}
