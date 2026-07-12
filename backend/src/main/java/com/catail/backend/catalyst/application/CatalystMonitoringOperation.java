package com.catail.backend.catalyst.application;

import java.time.LocalDateTime;
import java.util.List;

public record CatalystMonitoringOperation(
        String status,
        List<String> searchConditions,
        int searchIntervalHours,
        LocalDateTime lastSearchedAt,
        LocalDateTime activatedAt
) {
}
