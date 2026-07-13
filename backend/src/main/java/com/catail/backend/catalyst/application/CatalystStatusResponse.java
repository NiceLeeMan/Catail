package com.catail.backend.catalyst.application;

import java.time.LocalDateTime;

public record CatalystStatusResponse(
        String status,
        LocalDateTime updatedAt
) {
}
