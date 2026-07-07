package com.catail.backend.catalyst.application;

import java.time.LocalDateTime;
import java.util.List;

public record CatalystListItemResponse(
        Long id,
        String title,
        String status,
        List<String> industryTags,
        int pendingSignalCount,
        LocalDateTime createdAt
) {
}
