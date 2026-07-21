package com.catail.backend.company.outbound;

import com.catail.backend.company.domain.Market;

import java.time.LocalDate;

public interface ListedCompanyCollectionPort {

    Market market();

    ListedCompanyPage fetchPage(LocalDate baseDate, int pageNo, int numOfRows);
}
