package com.catail.backend.websearch.application;

import com.catail.backend.websearch.db.SearchResult;
import com.catail.backend.websearch.db.SearchResultRepository;
import com.catail.backend.websearch.domain.CrawlFailureCode;
import com.catail.backend.websearch.domain.CrawlStatus;
import com.catail.backend.websearch.outbound.JinaReadException;
import com.catail.backend.websearch.outbound.JinaReadResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CrawlResultRecorderTest {

    @Mock
    private SearchResultRepository searchResultRepository;

    private CrawlResultRecorder recorder;

    private void createRecorder(int maxRetryCount) {
        recorder = new CrawlResultRecorder(searchResultRepository, maxRetryCount);
    }

    private static SearchResult resultWithId(Long id) throws Exception {
        SearchResult result = SearchResult.create(1L, "제목", "https://example.com/a");
        Field field = SearchResult.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(result, id);
        return result;
    }

    @Test
    @DisplayName("content가 있고 warning이 없으면 SUCCESS로 저장한다")
    void recordOutcome_hasContent_marksSuccess() throws Exception {
        createRecorder(3);
        SearchResult result = resultWithId(1L);
        given(searchResultRepository.findById(1L)).willReturn(Optional.of(result));

        recorder.recordOutcome(1L, new JinaReadResult(null, "제목", "u", "본문", null, 10));

        assertThat(result.getCrawlStatus()).isEqualTo(CrawlStatus.SUCCESS);
        assertThat(result.getContent()).isEqualTo("본문");
        assertThat(result.getTokenCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("warning이 있고 content가 비어있으면 TARGET_PAGE_ERROR로 실패 처리한다")
    void recordOutcome_warningWithoutContent_marksTargetPageError() throws Exception {
        createRecorder(3);
        SearchResult result = resultWithId(1L);
        given(searchResultRepository.findById(1L)).willReturn(Optional.of(result));

        recorder.recordOutcome(1L, new JinaReadResult("접근 제한", "제목", "u", "", null, null));

        assertThat(result.getCrawlStatus()).isEqualTo(CrawlStatus.FAILED);
        assertThat(result.getFailureCode()).isEqualTo(CrawlFailureCode.TARGET_PAGE_ERROR);
        assertThat(result.getFailureDetail()).isEqualTo("접근 제한");
    }

    @Test
    @DisplayName("warning 없이 content만 비어있으면 EMPTY_CONTENT로 실패 처리한다")
    void recordOutcome_blankContentNoWarning_marksEmptyContent() throws Exception {
        createRecorder(3);
        SearchResult result = resultWithId(1L);
        given(searchResultRepository.findById(1L)).willReturn(Optional.of(result));

        recorder.recordOutcome(1L, new JinaReadResult(null, "제목", "u", "  ", null, null));

        assertThat(result.getCrawlStatus()).isEqualTo(CrawlStatus.FAILED);
        assertThat(result.getFailureCode()).isEqualTo(CrawlFailureCode.EMPTY_CONTENT);
    }

    @Test
    @DisplayName("재시도 가능한 예외이고 재시도 횟수가 남아있으면 PENDING으로 되돌리고 retryCount를 증가시킨다")
    void recordException_retryableUnderLimit_marksRetryPending() throws Exception {
        createRecorder(3);
        SearchResult result = resultWithId(1L);
        given(searchResultRepository.findById(1L)).willReturn(Optional.of(result));

        recorder.recordException(1L, new JinaReadException(CrawlFailureCode.TIMEOUT, true, "타임아웃"));

        assertThat(result.getCrawlStatus()).isEqualTo(CrawlStatus.PENDING);
        assertThat(result.getRetryCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("재시도 가능한 예외라도 재시도 횟수를 소진했으면 FAILED로 확정한다")
    void recordException_retryableExhausted_marksFailed() throws Exception {
        createRecorder(1);
        SearchResult result = resultWithId(1L);
        result.markCrawlRetryPending("이전 실패"); // retryCount = 1
        given(searchResultRepository.findById(1L)).willReturn(Optional.of(result));

        recorder.recordException(1L, new JinaReadException(CrawlFailureCode.NETWORK_ERROR, true, "네트워크 오류"));

        assertThat(result.getCrawlStatus()).isEqualTo(CrawlStatus.FAILED);
        assertThat(result.getFailureCode()).isEqualTo(CrawlFailureCode.NETWORK_ERROR);
    }

    @Test
    @DisplayName("재시도 불가능한 예외면 재시도 횟수와 무관하게 즉시 FAILED로 확정한다")
    void recordException_nonRetryable_marksFailedImmediately() throws Exception {
        createRecorder(3);
        SearchResult result = resultWithId(1L);
        given(searchResultRepository.findById(1L)).willReturn(Optional.of(result));

        recorder.recordException(1L, new JinaReadException(CrawlFailureCode.CLIENT_ERROR, false, "401"));

        assertThat(result.getCrawlStatus()).isEqualTo(CrawlStatus.FAILED);
        assertThat(result.getFailureCode()).isEqualTo(CrawlFailureCode.CLIENT_ERROR);
        assertThat(result.getRetryCount()).isEqualTo(0);
    }
}
