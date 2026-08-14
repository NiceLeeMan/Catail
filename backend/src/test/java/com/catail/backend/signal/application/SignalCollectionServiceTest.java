package com.catail.backend.signal.application;

import com.catail.backend.catalyst.db.Catalyst;
import com.catail.backend.catalyst.db.CatalystRepository;
import com.catail.backend.catalyst.domain.CatalystCategory;
import com.catail.backend.catalyst.domain.CatalystStatus;
import com.catail.backend.signal.db.Signal;
import com.catail.backend.signal.domain.NewsArticle;
import com.catail.backend.signal.outbound.naver.NaverNewsItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SignalCollectionServiceTest {

    private static final Long CATALYST_ID = 1L;
    private static final Long COMPANY_ID = 100L;

    @Mock private CatalystRepository catalystRepository;
    @Mock private SignalSearchQueryGenerationService signalSearchQueryGenerationService;
    @Mock private SignalNewsCollectionService signalNewsCollectionService;
    @Mock private NewsArticleCollectionService newsArticleCollectionService;
    @Mock private RelevanceStubFilter relevanceStubFilter;
    @Mock private SignalSaveService signalSaveService;

    private SignalCollectionService service() {
        return new SignalCollectionService(catalystRepository, signalSearchQueryGenerationService,
                signalNewsCollectionService, newsArticleCollectionService, relevanceStubFilter, signalSaveService);
    }

    private Catalyst catalyst(CatalystStatus status) {
        return Catalyst.create(1L, COMPANY_ID, CatalystCategory.SUPPLY_CHAIN,
                "테스트용 상세내용 10자 이상 작성", "공급망", status);
    }

    private NewsArticle article() {
        return new NewsArticle("제목", "요약", "https://origin.example.com/1", "https://n.news.naver.com/1",
                OffsetDateTime.parse("2026-08-10T09:00:00+09:00"), "연합뉴스", List.of("검색어1"));
    }

    @Test
    @DisplayName("ACTIVE이고 삭제되지 않은 카탈리스트면 전체 파이프라인을 실행해 Signal을 저장한다")
    void collectFor_activeCatalyst_runsPipelineAndSavesSignals() {
        Catalyst catalyst = catalyst(CatalystStatus.ACTIVE);
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.of(catalyst));
        given(signalSearchQueryGenerationService.generate(COMPANY_ID, "SUPPLY_CHAIN", catalyst.getDetail()))
                .willReturn(List.of("검색어1"));
        Map<String, List<NaverNewsItem>> rawResults = Map.of("검색어1", List.of());
        given(signalNewsCollectionService.collect(List.of("검색어1"))).willReturn(rawResults);
        NewsArticle article = article();
        given(newsArticleCollectionService.collect(CATALYST_ID, rawResults)).willReturn(List.of(article));
        given(relevanceStubFilter.filter(List.of(article))).willReturn(List.of(article));
        Signal savedSignal = Signal.create(CATALYST_ID, article.title(), article.description(),
                article.originallink(), article.link(), article.pubDate(), article.press());
        given(signalSaveService.save(CATALYST_ID, article)).willReturn(savedSignal);

        List<Signal> result = service().collectFor(CATALYST_ID);

        assertThat(result).containsExactly(savedSignal);
    }

    @Test
    @DisplayName("존재하지 않는 카탈리스트면 빈 목록을 반환하고 이후 단계를 실행하지 않는다")
    void collectFor_catalystNotFound_returnsEmptyListWithoutFurtherSteps() {
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.empty());

        List<Signal> result = service().collectFor(CATALYST_ID);

        assertThat(result).isEmpty();
        verifyNoInteractions(signalSearchQueryGenerationService, signalNewsCollectionService,
                newsArticleCollectionService, signalSaveService);
    }

    @Test
    @DisplayName("삭제된 카탈리스트면 빈 목록을 반환하고 이후 단계를 실행하지 않는다")
    void collectFor_deletedCatalyst_returnsEmptyListWithoutFurtherSteps() {
        Catalyst catalyst = catalyst(CatalystStatus.ACTIVE);
        catalyst.softDelete();
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.of(catalyst));

        List<Signal> result = service().collectFor(CATALYST_ID);

        assertThat(result).isEmpty();
        verifyNoInteractions(signalSearchQueryGenerationService);
    }

    @Test
    @DisplayName("ACTIVE가 아닌 카탈리스트면 빈 목록을 반환하고 이후 단계를 실행하지 않는다")
    void collectFor_notActiveCatalyst_returnsEmptyListWithoutFurtherSteps() {
        Catalyst catalyst = catalyst(CatalystStatus.PAUSED);
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.of(catalyst));

        List<Signal> result = service().collectFor(CATALYST_ID);

        assertThat(result).isEmpty();
        verifyNoInteractions(signalSearchQueryGenerationService);
    }

    @Test
    @DisplayName("생성된 검색어가 없으면 이후 단계 없이 빈 목록을 반환한다")
    void collectFor_noQueriesGenerated_returnsEmptyListWithoutFurtherSteps() {
        Catalyst catalyst = catalyst(CatalystStatus.ACTIVE);
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.of(catalyst));
        given(signalSearchQueryGenerationService.generate(COMPANY_ID, "SUPPLY_CHAIN", catalyst.getDetail()))
                .willReturn(List.of());

        List<Signal> result = service().collectFor(CATALYST_ID);

        assertThat(result).isEmpty();
        verifyNoInteractions(signalNewsCollectionService, newsArticleCollectionService, signalSaveService);
    }
}
