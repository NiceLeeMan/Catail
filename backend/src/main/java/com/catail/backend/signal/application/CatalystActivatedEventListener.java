package com.catail.backend.signal.application;

import com.catail.backend.catalyst.application.CatalystActivatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 카탈리스트 저장 결과가 ACTIVE가 되는 모든 경로(생성/상태변경)에서 발행되는 이벤트를 받아
 * 시그널 수집을 트리거한다. 상태변경(DB 트랜잭션)과 수집(외부 API 호출)을 분리하기 위해
 * AFTER_COMMIT 시점에만 실행하며, API 응답을 막지 않도록 별도 스레드(@Async)에서 실행한다.
 * 실패해도 별도 재시도는 하지 않는다 — 다음 폴링 tick이 안전망 역할을 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CatalystActivatedEventListener {

    private final SignalCollectionService signalCollectionService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCatalystActivated(CatalystActivatedEvent event) {
        try {
            signalCollectionService.collectFor(event.catalystId());
        } catch (Exception e) {
            log.error("카탈리스트 ACTIVE 전환 이벤트 처리 중 시그널 수집 실패 (catalystId={})", event.catalystId(), e);
        }
    }
}
