package com.catail.backend.company.inbound.read;

import org.springframework.data.domain.Page;

import java.util.List;

public record CompanyListResponse(
        List<CompanyListItem> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static CompanyListResponse from(Page<CompanyListItem> page) {
        return new CompanyListResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
