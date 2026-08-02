package com.catail.backend.searchplan.outbound;

import com.catail.backend.searchplan.domain.Criterion;
import com.catail.backend.searchplan.domain.TargetCompanyInfo;

import java.util.Map;

public interface SearchPlanLlmPort {

    Map<Criterion, CriterionLlmItem> generate(TargetCompanyInfo targetCompany, String analysisScope);
}
