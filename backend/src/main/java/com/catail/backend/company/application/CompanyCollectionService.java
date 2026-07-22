package com.catail.backend.company.application;

import com.catail.backend.company.db.Company;
import com.catail.backend.company.db.CompanyRepository;
import com.catail.backend.company.domain.Market;
import com.catail.backend.company.outbound.ListedCompanyCollectionException;
import com.catail.backend.company.outbound.ListedCompanyCollectionPort;
import com.catail.backend.company.outbound.ListedCompanyItem;
import com.catail.backend.company.outbound.ListedCompanyPage;
import com.catail.backend.global.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyCollectionService {

    private static final int MAX_LOOKBACK_DAYS = 10;
    private static final int PAGE_FETCH_SIZE = 1000;

    private final List<ListedCompanyCollectionPort> ports;
    private final CompanyRepository companyRepository;

    public void collect(Market market) {
        ListedCompanyCollectionPort port = resolvePort(market);
        try {
            LocalDate baseDate = resolveLatestAvailableBaseDate(port);
            List<ListedCompanyItem> items = fetchAllItems(port, baseDate);
            items.forEach(this::upsert);
            log.info("{} 상장기업 수집 완료: baseDate={}, count={}", market, baseDate, items.size());
        } catch (ListedCompanyCollectionException e) {
            throw new BusinessException(CompanyErrorCode.COLLECTION_SOURCE_ERROR);
        }
    }

    private ListedCompanyCollectionPort resolvePort(Market market) {
        return ports.stream()
                .filter(port -> port.market() == market)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("등록된 수집 Port가 없습니다: " + market));
    }

    private LocalDate resolveLatestAvailableBaseDate(ListedCompanyCollectionPort port) {
        LocalDate candidate = LocalDate.now();
        for (int i = 0; i < MAX_LOOKBACK_DAYS; i++) {
            ListedCompanyPage probe = port.fetchPage(candidate, 1, 1);
            if (probe.totalCount() > 0) {
                return candidate;
            }
            candidate = candidate.minusDays(1);
        }
        throw new BusinessException(CompanyErrorCode.COLLECTION_DATA_NOT_FOUND);
    }

    private List<ListedCompanyItem> fetchAllItems(ListedCompanyCollectionPort port, LocalDate baseDate) {
        List<ListedCompanyItem> result = new ArrayList<>();
        int pageNo = 1;
        ListedCompanyPage page = port.fetchPage(baseDate, pageNo, PAGE_FETCH_SIZE);
        result.addAll(page.items());

        int totalPages = (int) Math.ceil((double) page.totalCount() / PAGE_FETCH_SIZE);
        for (pageNo = 2; pageNo <= totalPages; pageNo++) {
            page = port.fetchPage(baseDate, pageNo, PAGE_FETCH_SIZE);
            result.addAll(page.items());
        }
        return result;
    }

    private void upsert(ListedCompanyItem item) {
        companyRepository.findByMarketAndStockCode(item.market(), item.stockCode())
                .ifPresentOrElse(
                        existing -> {
                            if (!existing.getCompanyName().equals(item.companyName())) {
                                existing.updateCompanyName(item.companyName());
                                companyRepository.save(existing);
                            }
                        },
                        () -> companyRepository.save(
                                Company.create(item.market(), item.stockCode(), item.companyName()))
                );
    }
}
