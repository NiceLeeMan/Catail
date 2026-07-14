package com.catail.backend.catalyst.inbound.update;

import java.time.LocalDateTime;
import java.util.List;

public record CatalystUpdateResponse(
        String title,
        String content,
        List<String> industries,
        LocalDateTime updatedAt
) {
}
