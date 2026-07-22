package com.catail.backend.disclosure.application;

import com.catail.backend.company.db.CompanyRepository;
import com.catail.backend.disclosure.db.Disclosure;
import com.catail.backend.disclosure.db.DisclosureRemark;
import com.catail.backend.disclosure.db.DisclosureRemarkRepository;
import com.catail.backend.disclosure.db.DisclosureRepository;
import com.catail.backend.disclosure.db.DisclosureSyncStatus;
import com.catail.backend.disclosure.db.DisclosureSyncStatusRepository;
import com.catail.backend.disclosure.domain.DisclosureProvider;
import com.catail.backend.disclosure.inbound.read.DisclosureItem;
import com.catail.backend.disclosure.inbound.read.DisclosureListResponse;
import com.catail.backend.global.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DisclosureListService {

    private final CompanyRepository companyRepository;
    private final DisclosureRepository disclosureRepository;
    private final DisclosureRemarkRepository disclosureRemarkRepository;
    private final DisclosureSyncStatusRepository disclosureSyncStatusRepository;

    @Transactional(readOnly = true)
    public DisclosureListResponse getList(Long companyId, String cursor, int size) {
        if (!companyRepository.existsById(companyId)) {
            throw new BusinessException(DisclosureErrorCode.COMPANY_NOT_FOUND);
        }

        DisclosureProvider provider = DisclosureProvider.OPEN_DART;
        Pageable pageable = PageRequest.of(0, size + 1);

        List<Disclosure> fetched = StringUtils.hasText(cursor)
                ? fetchNextPage(companyId, provider, cursor, pageable)
                : disclosureRepository.findFirstPage(companyId, provider, pageable);

        boolean hasNext = fetched.size() > size;
        List<Disclosure> pageItems = hasNext ? fetched.subList(0, size) : fetched;
        String nextCursor = hasNext ? buildNextCursor(pageItems.get(pageItems.size() - 1)) : null;

        Map<Long, List<String>> remarkCodesByDisclosureId = groupRemarkCodes(pageItems);
        List<DisclosureItem> items = pageItems.stream()
                .map(disclosure -> DisclosureItem.from(
                        disclosure, remarkCodesByDisclosureId.getOrDefault(disclosure.getId(), List.of())))
                .toList();

        return buildResponse(companyId, provider, items, nextCursor, hasNext);
    }

    private List<Disclosure> fetchNextPage(
            Long companyId, DisclosureProvider provider, String cursor, Pageable pageable) {
        DisclosureCursor decoded = DisclosureCursor.decode(cursor);
        return disclosureRepository.findNextPage(
                companyId, provider, decoded.receivedDate(), decoded.externalDisclosureId(), pageable);
    }

    private String buildNextCursor(Disclosure last) {
        return DisclosureCursor.encode(last.getReceivedDate(), last.getExternalDisclosureId());
    }

    private Map<Long, List<String>> groupRemarkCodes(List<Disclosure> disclosures) {
        List<Long> disclosureIds = disclosures.stream().map(Disclosure::getId).toList();
        if (disclosureIds.isEmpty()) {
            return Map.of();
        }
        return disclosureRemarkRepository.findByDisclosureIdIn(disclosureIds).stream()
                .collect(Collectors.groupingBy(
                        DisclosureRemark::getDisclosureId,
                        Collectors.mapping(DisclosureRemark::getCode, Collectors.toList())));
    }

    private DisclosureListResponse buildResponse(
            Long companyId, DisclosureProvider provider, List<DisclosureItem> items, String nextCursor, boolean hasNext) {
        return disclosureSyncStatusRepository.findByCompanyIdAndProvider(companyId, provider)
                .map(status -> new DisclosureListResponse(
                        items, nextCursor, hasNext, status.getLastSuccessfulSyncAt(), status.isInitialSyncCompleted()))
                .orElseGet(() -> new DisclosureListResponse(items, nextCursor, hasNext, null, false));
    }
}
