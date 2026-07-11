package com.catail.backend.catalyst.application;

import java.time.LocalDateTime;
import java.util.List;

public record CatalystCreateResponse(
        Long id,
        String title,
        String content,
        String status,
        List<String> industries,
        LocalDateTime createdAt
) {
}
