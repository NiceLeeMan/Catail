package com.catail.backend.company.inbound.read;

import com.catail.backend.company.db.Company;

public record CompanyDetailResponse(
        Long id,
        String market,
        String stockCode,
        String companyName,
        String industryName,
        String logoUrl
) {
    public static CompanyDetailResponse from(Company company) {
        return new CompanyDetailResponse(
                company.getId(),
                company.getMarket().name(),
                company.getStockCode(),
                company.getCompanyName(),
                company.getIndustryName(),
                company.getLogoUrl()
        );
    }
}
