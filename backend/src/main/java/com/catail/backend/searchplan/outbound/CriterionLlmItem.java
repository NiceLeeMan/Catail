package com.catail.backend.searchplan.outbound;

import java.util.List;

public record CriterionLlmItem(
        String scopeRelevance,
        List<String> queries
) {
}
