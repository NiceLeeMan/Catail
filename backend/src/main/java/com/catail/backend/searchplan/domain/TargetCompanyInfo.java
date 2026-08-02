package com.catail.backend.searchplan.domain;

import com.catail.backend.company.domain.Market;

public record TargetCompanyInfo(
        Long companyId,
        String companyName,
        String englishName,
        String stockCode,
        Market market,
        String countryCode
) {
}
