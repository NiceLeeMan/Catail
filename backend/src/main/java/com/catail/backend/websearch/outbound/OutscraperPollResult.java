package com.catail.backend.websearch.outbound;

import java.util.List;

public record OutscraperPollResult(
        OutscraperJobStatus status,
        List<OutscraperQueryResult> data
) {
}
