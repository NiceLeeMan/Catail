package com.catail.backend.signal.domain;

import java.time.OffsetDateTime;
import java.util.List;

public record NewsArticle(
        String title,
        String description,
        String originallink,
        String link,
        OffsetDateTime pubDate,
        String press,
        List<String> sourceQueries
) {
}
