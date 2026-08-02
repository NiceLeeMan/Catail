package com.catail.backend.searchplan.domain;

import java.util.List;

public record CriterionPlan(
        Criterion criterion,
        String scopeRelevance,
        List<String> queries
) {
}
