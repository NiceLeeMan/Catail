package com.catail.backend.catalyst.inbound.delete;

import java.time.LocalDateTime;

public record CatalystDeleteResponse(
        String status,
        LocalDateTime deletedAt
) {
}
