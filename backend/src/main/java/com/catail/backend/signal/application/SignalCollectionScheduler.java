package com.catail.backend.signal.application;

import com.catail.backend.catalyst.db.Catalyst;
import com.catail.backend.catalyst.db.CatalystRepository;
import com.catail.backend.catalyst.domain.CatalystStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
public class SignalCollectionScheduler implements ApplicationRunner {

    private final CatalystRepository catalystRepository;
    private final SignalCollectionService signalCollectionService;

    @Value("${signal.collection.run-on-startup:false}")
    private boolean runOnStartup;

    @Override
    public void run(ApplicationArguments args) {
        if (!runOnStartup) {
            log.info("signal.collection.run-on-startup=false, 기동 시 시그널 수집을 건너뜁니다.");
            return;
        }
        collect();
    }

    @Scheduled(cron = "${signal.collection.polling-cron:0 */30 * * * *}")
    public void collect() {
        List<Catalyst> targets = catalystRepository.findAllByStatus(CatalystStatus.ACTIVE);
        for (Catalyst catalyst : targets) {
            try {
                signalCollectionService.collectFor(catalyst.getId());
            } catch (Exception e) {
                log.error("시그널 폴링 수집 실패 (catalystId={})", catalyst.getId(), e);
            }
        }
    }
}
