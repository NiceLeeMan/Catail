package com.catail.backend.company.application;

import com.catail.backend.company.domain.Market;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
@Order(1) // DisclosureSyncScheduler가 corp_code 매핑 이후에 기동 시 실행되도록 순서 고정
public class CompanyCollectionScheduler implements ApplicationRunner {

    private final CompanyCollectionService companyCollectionService;
    private final CompanyOpenDartMappingService companyOpenDartMappingService;

    @Value("${company.collection.run-on-startup:true}")
    private boolean runOnStartup;

    @Override
    public void run(ApplicationArguments args) {
        if (!runOnStartup) {
            log.info("company.collection.run-on-startup=false, 기동 시 기업 수집을 건너뜁니다.");
            return;
        }
        collectKospi();
        collectNasdaq();
    }

    @Scheduled(cron = "0 0 15 * * *")
    public void collectKospi() {
        try {
            companyCollectionService.collect(Market.KOSPI);
            companyOpenDartMappingService.mapKospiCorpCodes();
        } catch (Exception e) {
            log.error("코스피 상장기업 수집 실패", e);
        }
    }

    @Scheduled(cron = "0 30 15 * * *")
    public void collectNasdaq() {
        try {
            companyCollectionService.collect(Market.NASDAQ);
        } catch (Exception e) {
            log.error("나스닥 상장기업 수집 실패", e);
        }
    }
}
