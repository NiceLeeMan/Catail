package com.catail.backend.websearch.application;

import com.catail.backend.websearch.db.SearchResult;
import com.catail.backend.websearch.domain.CrawlFailureCode;
import com.catail.backend.websearch.outbound.JinaReadException;
import com.catail.backend.websearch.outbound.JinaReadResult;
import com.catail.backend.websearch.outbound.JinaReaderPort;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * PENDING 상태의 SearchResult를 제한된 동시성으로 Jina Reader에 병렬 요청해 원문을 수집한다.
 * RestClient가 blocking이라 고정 크기 스레드풀로 동시 요청 수를 제한한다.
 */
@Slf4j
@Component
@Profile("!test")
public class CrawlDispatchService {

    private final CrawlCandidateSelector crawlCandidateSelector;
    private final JinaReaderPort jinaReaderPort;
    private final CrawlResultRecorder crawlResultRecorder;
    private final int maxConcurrentRequests;

    private ExecutorService executor;

    public CrawlDispatchService(
            CrawlCandidateSelector crawlCandidateSelector,
            JinaReaderPort jinaReaderPort,
            CrawlResultRecorder crawlResultRecorder,
            @Value("${jina.api.max-concurrent-requests}") int maxConcurrentRequests
    ) {
        this.crawlCandidateSelector = crawlCandidateSelector;
        this.jinaReaderPort = jinaReaderPort;
        this.crawlResultRecorder = crawlResultRecorder;
        this.maxConcurrentRequests = maxConcurrentRequests;
    }

    @PostConstruct
    public void init() {
        this.executor = Executors.newFixedThreadPool(maxConcurrentRequests);
        // 재시작 복구는 반드시 첫 @Scheduled 실행보다 먼저 끝나야 한다. Spring은 모든 빈의
        // @PostConstruct를 컨텍스트 리프레시 도중(빈 초기화 단계)에 마친 뒤에야 @Scheduled 트리거를
        // 시작시키므로, 여기서 동기 호출하면 순서가 보장된다. crawlCandidateSelector는 DI로 주입된
        // 별도 빈이라 이 호출은 프록시를 정상적으로 거쳐 @Transactional이 적용된다(self-invocation 아님).
        crawlCandidateSelector.recoverStaleProcessing();
    }

    @PreDestroy
    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void dispatchPendingCrawls() {
        List<SearchResult> batch = crawlCandidateSelector.claimNextBatch(maxConcurrentRequests);
        if (batch.isEmpty()) {
            return;
        }

        List<CompletableFuture<Void>> futures = batch.stream()
                .map(candidate -> CompletableFuture.runAsync(() -> processOne(candidate), executor))
                .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private void processOne(SearchResult candidate) {
        try {
            JinaReadResult result = jinaReaderPort.read(candidate.getCrawlUrl());
            crawlResultRecorder.recordOutcome(candidate.getId(), result);
        } catch (JinaReadException e) {
            crawlResultRecorder.recordException(candidate.getId(), e);
        } catch (Exception e) {
            log.error("SearchResult 크롤링 중 예상치 못한 예외 발생: id={}", candidate.getId(), e);
            crawlResultRecorder.recordException(
                    candidate.getId(), new JinaReadException(CrawlFailureCode.UNKNOWN_ERROR, false, e.getMessage(), e));
        }
    }
}
