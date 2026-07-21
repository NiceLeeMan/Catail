package com.catail.backend.company.outbound;

import com.catail.backend.company.domain.Market;

public record ListedCompanyItem(
        Market market,
        String stockCode,
        String companyName
) {
}
