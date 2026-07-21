package com.catail.backend.company.application;

import com.catail.backend.company.domain.Market;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class CompanyCollectionScheduler implements ApplicationRunner {

    private final CompanyCollectionService companyCollectionService;

    @Override
    public void run(ApplicationArguments args) {
        collectKospi();
    }

    @Scheduled(cron = "0 0 15 * * *")
    public void collectKospi() {
        try {
            companyCollectionService.collect(Market.KOSPI);
        } catch (Exception e) {
            log.error("코스피 상장기업 수집 실패", e);
        }
    }
}
