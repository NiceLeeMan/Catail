package com.catail.backend.catalyst.application;

import java.time.LocalDateTime;
import java.util.List;

public record CatalystUpdateResponse(
        String title,
        String content,
        List<String> industries,
        LocalDateTime updatedAt
) {
}
