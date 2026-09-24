package com.catail.backend.signal.application;

import com.catail.backend.catalyst.db.Catalyst;
import com.catail.backend.catalyst.db.CatalystRepository;
import com.catail.backend.catalyst.domain.CatalystStatus;
import com.catail.backend.signal.db.Signal;
import com.catail.backend.signal.domain.NewsArticle;
import com.catail.backend.signal.outbound.naver.NaverNewsItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <h3>시그널 수집 파이프라인 오케스트레이션 서비스</h3>
 *
 * <p>카탈리스트를 기준으로 검색어 생성, 뉴스 수집, 기사 변환,
 * 관련성 필터링 및 시그널 저장 과정을 순서대로 조율한다.</p>
 *
 * <p>각 단계의 구체적인 처리 책임은 개별 서비스에 위임하며,
 * 이벤트 기반 트리거와 폴링 기반 트리거가 공통으로 사용하는 진입점을 제공한다.</p>
 */
@Service
@RequiredArgsConstructor
public class SignalCollectionService {

    private final CatalystRepository catalystRepository;
    private final SignalSearchQueryGenerationService signalSearchQueryGenerationService;
    private final SignalNewsCollectionService signalNewsCollectionService;
    private final NewsArticleCollectionService newsArticleCollectionService;
    private final RelevanceStubFilter relevanceStubFilter;
    private final SignalSaveService signalSaveService;

    /**
     * 지정된 카탈리스트의 시그널 수집 파이프라인을 오케스트레이션한다.
     *
     * <p>수집 대상 확인부터 검색어 생성, 뉴스 수집, 기사 변환,
     * 관련성 필터링 및 시그널 저장까지의 실행 순서와 중단 조건을 관리한다.</p>
     *
     * <p>카탈리스트가 존재하지 않거나 수집 대상이 아닌 경우,
     * 또는 생성된 검색어가 없는 경우 빈 목록을 반환한다.</p>
     *
     * @param catalystId 시그널을 수집할 카탈리스트 식별자
     * @return 수집 및 저장된 시그널 목록
     */
    public List<Signal> collectFor(Long catalystId) {
        Catalyst catalyst = catalystRepository.findById(catalystId).orElse(null);
        if (!isCollectionTarget(catalyst)) {
            return List.of();
        }

        List<String> queries = signalSearchQueryGenerationService.generate(
                catalyst.getCompanyId(), catalyst.getCategory().name(), catalyst.getDetail());
        if (queries.isEmpty()) {
            return List.of();
        }

        Map<String, List<NaverNewsItem>> rawResultsByQuery = signalNewsCollectionService.collect(queries);
        List<NewsArticle> articles = newsArticleCollectionService.collect(catalystId, rawResultsByQuery);
        List<NewsArticle> relevantArticles = relevanceStubFilter.filter(articles);

        return relevantArticles.stream()
                .map(article -> signalSaveService.save(catalystId, article))
                .toList();
    }

    /**
     * 카탈리스트가 현재 시그널 수집 대상인지 확인한다.
     *
     * @param catalyst 확인할 카탈리스트
     * @return 존재하며 삭제되지 않은 ACTIVE 상태이면 {@code true}
     */
    private boolean isCollectionTarget(Catalyst catalyst) {
        return catalyst != null && catalyst.getDeletedAt() == null && catalyst.getStatus() == CatalystStatus.ACTIVE;
    }
}
