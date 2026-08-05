package com.catail.backend.websearch.outbound;

import java.time.LocalDateTime;

public record JinaReadResult(
        String warning,
        String title,
        String url,
        String content,
        LocalDateTime publishedTime,
        Integer tokens
) {
}
