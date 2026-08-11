package com.catail.backend.catalyst.inbound;

import com.catail.backend.catalyst.db.Catalyst;

import java.time.LocalDateTime;

public record CatalystStatusResponse(
        Long catalystId,
        String status,
        LocalDateTime updatedAt
) {
    public static CatalystStatusResponse from(Catalyst catalyst) {
        return new CatalystStatusResponse(
                catalyst.getId(),
                catalyst.getStatus().name(),
                catalyst.getUpdatedAt()
        );
    }
}
