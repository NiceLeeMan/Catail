package com.catail.backend.signal.inbound;

import com.catail.backend.signal.db.Signal;

import java.time.LocalDateTime;

public record SignalStatusResponse(
        Long signalId,
        String status,
        LocalDateTime updatedAt
) {
    public static SignalStatusResponse from(Signal signal) {
        return new SignalStatusResponse(signal.getId(), signal.getStatus().name(), signal.getUpdatedAt());
    }
}
