package com.catail.backend.catalyst.application;

import java.time.LocalDateTime;

public record CatalystDeleteResponse(
        String status,
        LocalDateTime deletedAt
) {
}
