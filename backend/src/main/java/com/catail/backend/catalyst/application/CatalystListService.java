package com.catail.backend.catalyst.application;

import com.catail.backend.catalyst.db.CatalystRepositoryAdapter;
import com.catail.backend.catalyst.domain.CatalystDomain;
import com.catail.backend.catalyst.inbound.read.CatalystListItem;
import com.catail.backend.catalyst.inbound.read.CatalystListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CatalystListService {

    private static final int PAGE_SIZE = 20;

    private final CatalystRepositoryAdapter catalystRepositoryAdapter;

    @Transactional(readOnly = true)
    public CatalystListResponse getList(Long userId, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CatalystDomain> catalystPage = catalystRepositoryAdapter.findPageByUserId(userId, pageable);
        List<CatalystDomain> catalysts = catalystPage.getContent();

        Map<Long, String> industryNames = loadIndustryNames(catalysts);
        Map<Long, Integer> pendingSignalCounts = loadPendingSignalCounts(catalysts);
        List<CatalystListItem> items = toListItems(
                catalysts, industryNames, pendingSignalCounts);

        return toListResponse(catalystPage, items);
    }

    private Map<Long, String> loadIndustryNames(List<CatalystDomain> catalysts) {
        List<Long> distinctIndustryIds = catalysts.stream()
                .flatMap(catalyst -> catalyst.getIndustryIds().stream())
                .distinct()
                .toList();

        return catalystRepositoryAdapter.findIndustryNamesByIds(distinctIndustryIds);
    }

    private Map<Long, Integer> loadPendingSignalCounts(List<CatalystDomain> catalysts) {
        List<Long> catalystIds = catalysts.stream()
                .map(CatalystDomain::getId)
                .toList();

        return catalystRepositoryAdapter.countPendingSignalsByCatalystIds(catalystIds);
    }

    private List<CatalystListItem> toListItems(
            List<CatalystDomain> catalysts,
            Map<Long, String> industryNamesById,
            Map<Long, Integer> pendingSignalCountsByCatalystId
    ) {
        return catalysts.stream()
                .map(catalyst -> CatalystListItem.from(
                        catalyst,
                        catalyst.getIndustryIds().stream().map(industryNamesById::get).toList(),
                        pendingSignalCountsByCatalystId.getOrDefault(catalyst.getId(), 0)
                ))
                .toList();
    }

    private CatalystListResponse toListResponse(
            Page<CatalystDomain> catalystPage,
            List<CatalystListItem> items
    ) {
        return new CatalystListResponse(
                items,
                catalystPage.getNumber(),
                catalystPage.getSize(),
                catalystPage.getTotalElements(),
                catalystPage.getTotalPages(),
                catalystPage.hasNext()
        );
    }
}
