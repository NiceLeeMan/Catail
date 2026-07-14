package com.catail.backend.catalyst.inbound.update;

import java.time.LocalDateTime;

public record CatalystStatusResponse(
        String status,
        LocalDateTime updatedAt
) {
}
