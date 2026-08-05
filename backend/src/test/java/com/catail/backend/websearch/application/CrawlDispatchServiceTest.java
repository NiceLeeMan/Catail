package com.catail.backend.websearch.application;

import com.catail.backend.websearch.db.SearchResult;
import com.catail.backend.websearch.domain.CrawlFailureCode;
import com.catail.backend.websearch.outbound.JinaReadException;
import com.catail.backend.websearch.outbound.JinaReadResult;
import com.catail.backend.websearch.outbound.JinaReaderPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CrawlDispatchServiceTest {

    @Mock
    private CrawlCandidateSelector crawlCandidateSelector;

    @Mock
    private JinaReaderPort jinaReaderPort;

    @Mock
    private CrawlResultRecorder crawlResultRecorder;

    private CrawlDispatchService service;

    private void createService(int maxConcurrentRequests) {
        service = new CrawlDispatchService(crawlCandidateSelector, jinaReaderPort, crawlResultRecorder, maxConcurrentRequests);
        service.init();
    }

    @AfterEach
    void tearDown() {
        if (service != null) {
            service.shutdown();
        }
    }

    private static SearchResult resultWithId(Long id, String url) throws Exception {
        SearchResult result = SearchResult.create(1L, "제목", url);
        Field field = SearchResult.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(result, id);
        return result;
    }

    @Test
    @DisplayName("init()은 스케줄 실행 전에 재시작 복구를 호출한다")
    void init_callsRecoverStaleProcessing() {
        createService(2);

        verify(crawlCandidateSelector).recoverStaleProcessing();
    }

    @Test
    @DisplayName("claimNextBatch가 빈 목록이면 아무 것도 처리하지 않는다")
    void dispatchPendingCrawls_emptyBatch_doesNothing() {
        createService(2);
        given(crawlCandidateSelector.claimNextBatch(2)).willReturn(List.of());

        service.dispatchPendingCrawls();

        verifyNoInteractions(jinaReaderPort);
        verifyNoInteractions(crawlResultRecorder);
    }

    @Test
    @DisplayName("각 후보를 병렬로 Jina에 요청하고 결과를 개별 기록한다")
    void dispatchPendingCrawls_processesEachCandidate() throws Exception {
        createService(2);
        SearchResult r1 = resultWithId(1L, "https://example.com/a");
        SearchResult r2 = resultWithId(2L, "https://example.com/b");
        given(crawlCandidateSelector.claimNextBatch(2)).willReturn(List.of(r1, r2));
        JinaReadResult read1 = new JinaReadResult(null, "t", "u", "본문1", null, 1);
        JinaReadResult read2 = new JinaReadResult(null, "t", "u", "본문2", null, 2);
        given(jinaReaderPort.read("https://example.com/a")).willReturn(read1);
        given(jinaReaderPort.read("https://example.com/b")).willReturn(read2);

        service.dispatchPendingCrawls();

        verify(crawlResultRecorder).recordOutcome(1L, read1);
        verify(crawlResultRecorder).recordOutcome(2L, read2);
    }

    @Test
    @DisplayName("한 후보가 JinaReadException을 던져도 다른 후보는 계속 처리된다")
    void dispatchPendingCrawls_oneCandidateThrowsJinaException_othersStillProcessed() throws Exception {
        createService(2);
        SearchResult failing = resultWithId(1L, "https://example.com/a");
        SearchResult healthy = resultWithId(2L, "https://example.com/b");
        given(crawlCandidateSelector.claimNextBatch(2)).willReturn(List.of(failing, healthy));
        JinaReadException exception = new JinaReadException(CrawlFailureCode.TIMEOUT, true, "타임아웃");
        given(jinaReaderPort.read("https://example.com/a")).willThrow(exception);
        JinaReadResult healthyResult = new JinaReadResult(null, "t", "u", "본문", null, 1);
        given(jinaReaderPort.read("https://example.com/b")).willReturn(healthyResult);

        service.dispatchPendingCrawls();

        verify(crawlResultRecorder).recordException(1L, exception);
        verify(crawlResultRecorder).recordOutcome(2L, healthyResult);
    }

    @Test
    @DisplayName("예상치 못한 예외는 UNKNOWN_ERROR로 감싸서 기록한다")
    void dispatchPendingCrawls_unexpectedException_wrapsAsUnknownError() throws Exception {
        createService(2);
        SearchResult candidate = resultWithId(1L, "https://example.com/a");
        given(crawlCandidateSelector.claimNextBatch(2)).willReturn(List.of(candidate));
        given(jinaReaderPort.read("https://example.com/a")).willThrow(new RuntimeException("예기치 못한 오류"));

        service.dispatchPendingCrawls();

        ArgumentCaptor<JinaReadException> captor = ArgumentCaptor.forClass(JinaReadException.class);
        verify(crawlResultRecorder).recordException(eq(1L), captor.capture());
        assertThat(captor.getValue().getFailureCode()).isEqualTo(CrawlFailureCode.UNKNOWN_ERROR);
        assertThat(captor.getValue().isRetryable()).isFalse();
    }
}
