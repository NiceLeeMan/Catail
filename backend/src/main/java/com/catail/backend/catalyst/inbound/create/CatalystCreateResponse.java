package com.catail.backend.catalyst.inbound.create;

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
