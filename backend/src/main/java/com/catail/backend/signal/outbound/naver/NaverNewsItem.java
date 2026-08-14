package com.catail.backend.signal.outbound.naver;

public record NaverNewsItem(
        String title,
        String originallink,
        String link,
        String description,
        String pubDate
) {
}
