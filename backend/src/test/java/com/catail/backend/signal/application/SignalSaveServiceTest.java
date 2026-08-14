package com.catail.backend.signal.application;

import com.catail.backend.signal.db.Signal;
import com.catail.backend.signal.db.SignalRepository;
import com.catail.backend.signal.db.SignalSourceQuery;
import com.catail.backend.signal.db.SignalSourceQueryRepository;
import com.catail.backend.signal.domain.NewsArticle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SignalSaveServiceTest {

    @Mock private SignalRepository signalRepository;
    @Mock private SignalSourceQueryRepository signalSourceQueryRepository;

    private SignalSaveService service() {
        return new SignalSaveService(signalRepository, signalSourceQueryRepository);
    }

    private NewsArticle article(List<String> sourceQueries) {
        return new NewsArticle("제목", "요약", "https://origin.example.com/1", "https://n.news.naver.com/1",
                OffsetDateTime.parse("2026-08-10T09:00:00+09:00"), "연합뉴스", sourceQueries);
    }

    @Test
    @DisplayName("NewsArticle을 Signal로 저장하고 sourceQueries 개수만큼 SignalSourceQuery를 저장한다")
    void save_persistsSignalAndAllSourceQueries() {
        NewsArticle article = article(List.of("검색어1", "검색어2"));
        Signal saved = Signal.create(1L, article.title(), article.description(), article.originallink(),
                article.link(), article.pubDate(), article.press());
        given(signalRepository.save(any(Signal.class))).willReturn(saved);

        Signal result = service().save(1L, article);

        assertThat(result).isSameAs(saved);
        verify(signalSourceQueryRepository, times(2)).save(any(SignalSourceQuery.class));
    }

    @Test
    @DisplayName("sourceQueries가 비어 있으면 SignalSourceQuery는 저장하지 않는다")
    void save_noSourceQueries_savesNoSourceQuery() {
        NewsArticle article = article(List.of());
        Signal saved = Signal.create(1L, article.title(), article.description(), article.originallink(),
                article.link(), article.pubDate(), article.press());
        given(signalRepository.save(any(Signal.class))).willReturn(saved);

        service().save(1L, article);

        verify(signalSourceQueryRepository, times(0)).save(any(SignalSourceQuery.class));
    }
}
