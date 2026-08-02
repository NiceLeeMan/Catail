package com.catail.backend.searchplan.domain;

import java.util.List;

public record SearchPlanFormat(
        TargetCompanyInfo targetCompany,
        String analysisScope,
        List<CriterionPlan> criteria
) {
}
