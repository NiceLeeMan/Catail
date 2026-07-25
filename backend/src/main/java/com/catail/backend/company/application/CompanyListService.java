package com.catail.backend.company.application;

import com.catail.backend.company.db.Company;
import com.catail.backend.company.db.CompanyRepository;
import com.catail.backend.company.domain.Market;
import com.catail.backend.company.inbound.read.CompanyListItem;
import com.catail.backend.company.inbound.read.CompanyListResponse;
import com.catail.backend.global.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CompanyListService {

    private static final int PAGE_SIZE = 50;
    private static final Set<Market> SUPPORTED_MARKETS = Set.of(Market.KOSPI, Market.NASDAQ);

    private final CompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public CompanyListResponse getList(String rawMarket, String keyword, int page) {
        Market market = parseSupportedMarket(rawMarket);
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.ASC, "stockCode"));

        String normalizedKeyword = normalizeKeyword(keyword);
        Page<Company> companyPage = normalizedKeyword == null
                ? companyRepository.findByMarket(market, pageable)
                : companyRepository.searchByKeyword(market, normalizedKeyword, pageable);
        Page<CompanyListItem> itemPage = companyPage.map(CompanyListItem::from);

        return CompanyListResponse.from(itemPage);
    }

    private Market parseSupportedMarket(String rawMarket) {
        Market market;
        try {
            market = Market.valueOf(rawMarket);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(CompanyErrorCode.UNSUPPORTED_MARKET);
        }
        if (!SUPPORTED_MARKETS.contains(market)) {
            throw new BusinessException(CompanyErrorCode.UNSUPPORTED_MARKET);
        }
        return market;
    }

    private String normalizeKeyword(String keyword) {
        return StringUtils.hasText(keyword) ? keyword.trim() : null;
    }
}
