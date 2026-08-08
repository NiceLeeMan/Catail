package com.catail.backend.crawlresultprove.inbound.read;

import com.catail.backend.crawlresultprove.db.CrawlResultProve;

import java.time.LocalDateTime;

public record CrawlResultProveResponse(
        Long id,
        Long searchResultId,
        int version,
        String content,
        LocalDateTime createdAt
) {
    public static CrawlResultProveResponse from(CrawlResultProve entity) {
        return new CrawlResultProveResponse(
                entity.getId(),
                entity.getSearchResultId(),
                entity.getVersion(),
                entity.getContent(),
                entity.getCreatedAt());
    }
}
