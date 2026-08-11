package com.catail.backend.catalyst.inbound;

import com.catail.backend.catalyst.db.Catalyst;
import com.catail.backend.company.db.Company;

import java.time.LocalDateTime;

public record CatalystUpdateResponse(
        Long catalystId,
        String title,
        String market,
        String stockCode,
        String category,
        String detail,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CatalystUpdateResponse from(Catalyst catalyst, Company company) {
        return new CatalystUpdateResponse(
                catalyst.getId(),
                catalyst.getTitle(),
                company.getMarket().name(),
                company.getStockCode(),
                catalyst.getCategory().name(),
                catalyst.getDetail(),
                catalyst.getStatus().name(),
                catalyst.getCreatedAt(),
                catalyst.getUpdatedAt()
        );
    }
}
