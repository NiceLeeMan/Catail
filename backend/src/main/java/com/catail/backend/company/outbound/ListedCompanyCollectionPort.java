package com.catail.backend.company.outbound;

import com.catail.backend.company.domain.Market;

import java.time.LocalDate;
import java.util.List;

public interface ListedCompanyCollectionPort {

    Market market();

    CollectionMode mode();

    default ListedCompanyPage fetchPage(LocalDate baseDate, int pageNo, int numOfRows) {
        throw new UnsupportedOperationException(market() + " 어댑터는 fetchPage를 지원하지 않습니다.");
    }

    default List<ListedCompanyItem> fetchSnapshot() {
        throw new UnsupportedOperationException(market() + " 어댑터는 fetchSnapshot을 지원하지 않습니다.");
    }
}
