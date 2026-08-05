package com.catail.backend.websearch.outbound;

import java.util.List;

public record OutscraperQueryResult(
        String query,
        List<OutscraperOrganicResult> organicResults
) {
}
