package com.catail.backend.searchplan.application;

public record SearchPlanRequest(
        Long companyId,
        String analysisScope
) {
}
