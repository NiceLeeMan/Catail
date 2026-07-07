package com.catail.backend.catalyst.outbound;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NoOpSignalCollectionAdapter implements SignalCollectionPort {

    // TODO(M5): 실제 시그널 후보 수집 파이프라인 연동
    @Override
    public void triggerCollection(Long catalystId) {
        log.info("Signal collection trigger stub called for catalystId={}", catalystId);
    }
}
