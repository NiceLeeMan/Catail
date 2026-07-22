package com.catail.backend.disclosure.outbound;

import com.catail.backend.disclosure.domain.DisclosureProvider;

import java.time.LocalDate;

public interface DisclosureCollectionPort {

    DisclosureProvider provider();

    DisclosureCollectionPage fetchPage(
            String externalCompanyId,
            LocalDate beginDate,
            LocalDate endDate,
            int pageNo,
            int pageSize
    );
}
