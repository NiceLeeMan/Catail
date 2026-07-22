package com.catail.backend.disclosure.application;

import com.catail.backend.company.db.Company;
import com.catail.backend.company.db.CompanyRepository;
import com.catail.backend.company.domain.Market;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DisclosureSyncScheduler implements ApplicationRunner {

    private final DisclosureSyncService disclosureSyncService;
    private final CompanyRepository companyRepository;

    @Override
    public void run(ApplicationArguments args) {
        syncKospiDisclosures();
    }

    @Scheduled(cron = "0 30 15 * * *")
    public void syncKospiDisclosures() {
        List<Company> companies = companyRepository.findByMarketAndOpenDartCorpCodeIsNotNull(Market.KOSPI);
        for (Company company : companies) {
            try {
                disclosureSyncService.syncCompany(company);
            } catch (Exception e) {
                log.error("공시 동기화 실패: companyId={}", company.getId(), e);
            }
        }
    }
}
