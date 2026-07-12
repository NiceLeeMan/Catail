package com.catail.backend.catalyst.application;

import java.time.LocalDateTime;
import java.util.List;

public record CatalystBasicInfo(
        String title,
        String content,
        List<String> industries,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
