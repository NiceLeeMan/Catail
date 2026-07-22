package com.catail.backend.disclosure.application;

import com.catail.backend.company.db.Company;
import com.catail.backend.disclosure.db.Disclosure;
import com.catail.backend.disclosure.db.DisclosureRemark;
import com.catail.backend.disclosure.db.DisclosureRemarkRepository;
import com.catail.backend.disclosure.db.DisclosureRepository;
import com.catail.backend.disclosure.db.DisclosureSyncStatus;
import com.catail.backend.disclosure.db.DisclosureSyncStatusRepository;
import com.catail.backend.disclosure.domain.DisclosureProvider;
import com.catail.backend.disclosure.outbound.DisclosureCollectionPage;
import com.catail.backend.disclosure.outbound.DisclosureCollectionPort;
import com.catail.backend.disclosure.outbound.RawDisclosureItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisclosureSyncService {

    private static final int INITIAL_SYNC_LOOKBACK_YEARS = 3;
    private static final int INCREMENTAL_SYNC_LOOKBACK_DAYS = 7;
    private static final int PAGE_FETCH_SIZE = 100;

    private final List<DisclosureCollectionPort> ports;
    private final DisclosureRepository disclosureRepository;
    private final DisclosureRemarkRepository disclosureRemarkRepository;
    private final DisclosureSyncStatusRepository disclosureSyncStatusRepository;

    public void syncCompany(Company company) {
        if (company.getOpenDartCorpCode() == null) {
            return;
        }

        DisclosureProvider provider = DisclosureProvider.OPEN_DART;
        DisclosureCollectionPort port = resolvePort(provider);
        DisclosureSyncStatus status = disclosureSyncStatusRepository
                .findByCompanyIdAndProvider(company.getId(), provider)
                .orElseGet(() -> DisclosureSyncStatus.create(company.getId(), provider));

        LocalDate endDate = LocalDate.now();
        LocalDate beginDate = status.isInitialSyncCompleted()
                ? endDate.minusDays(INCREMENTAL_SYNC_LOOKBACK_DAYS)
                : endDate.minusYears(INITIAL_SYNC_LOOKBACK_YEARS);

        List<RawDisclosureItem> items = fetchAllItems(port, company.getOpenDartCorpCode(), beginDate, endDate);
        items.forEach(item -> upsert(company.getId(), provider, item));

        status.markSuccess();
        disclosureSyncStatusRepository.save(status);
        log.info("공시 동기화 완료: companyId={}, provider={}, count={}", company.getId(), provider, items.size());
    }

    private DisclosureCollectionPort resolvePort(DisclosureProvider provider) {
        return ports.stream()
                .filter(port -> port.provider() == provider)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("등록된 공시 수집 Port가 없습니다: " + provider));
    }

    private List<RawDisclosureItem> fetchAllItems(
            DisclosureCollectionPort port, String externalCompanyId, LocalDate beginDate, LocalDate endDate) {
        List<RawDisclosureItem> result = new ArrayList<>();
        int pageNo = 1;
        DisclosureCollectionPage page = port.fetchPage(externalCompanyId, beginDate, endDate, pageNo, PAGE_FETCH_SIZE);
        result.addAll(page.items());

        int totalPages = (int) Math.ceil((double) page.totalCount() / PAGE_FETCH_SIZE);
        for (pageNo = 2; pageNo <= totalPages; pageNo++) {
            page = port.fetchPage(externalCompanyId, beginDate, endDate, pageNo, PAGE_FETCH_SIZE);
            result.addAll(page.items());
        }
        return result;
    }

    private void upsert(Long companyId, DisclosureProvider provider, RawDisclosureItem item) {
        Optional<Disclosure> existing = disclosureRepository
                .findByProviderAndExternalDisclosureId(provider, item.externalDisclosureId());

        Disclosure disclosure;
        if (existing.isPresent()) {
            disclosure = existing.get();
            boolean changed = !disclosure.getReportName().equals(item.reportName())
                    || !disclosure.getSubmitterName().equals(item.submitterName());
            if (changed) {
                disclosure.updateContent(item.reportName(), item.submitterName());
                disclosureRepository.save(disclosure);
            }
        } else {
            disclosure = disclosureRepository.save(Disclosure.create(
                    companyId, provider, item.externalDisclosureId(),
                    item.receivedDate(), item.reportName(), item.submitterName()));
        }

        for (String code : item.remarkCodes()) {
            if (!disclosureRemarkRepository.existsByDisclosureIdAndCode(disclosure.getId(), code)) {
                disclosureRemarkRepository.save(DisclosureRemark.create(disclosure.getId(), code));
            }
        }
    }
}
