package com.catail.backend.company.outbound;

import java.util.List;

public record ListedCompanyPage(
        int totalCount,
        List<ListedCompanyItem> items
) {
}
