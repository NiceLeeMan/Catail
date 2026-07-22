package com.catail.backend.company.inbound.read;

import com.catail.backend.company.db.Company;

public record CompanyListItem(
        Long id,
        String companyName,
        String stockCode,
        String market,
        String industryName,
        String logoUrl
) {
    public static CompanyListItem from(Company company) {
        return new CompanyListItem(
                company.getId(),
                company.getCompanyName(),
                company.getStockCode(),
                company.getMarket().name(),
                company.getIndustryName(),
                company.getLogoUrl()
        );
    }
}
