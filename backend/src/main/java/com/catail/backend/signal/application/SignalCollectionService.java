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
 * 시그널 수집 파이프라인 전체를 조립하는 진입점. 이벤트 기반 트리거(카탈리스트 ACTIVE 전환)와
 * 폴링 기반 트리거 둘 다 이 서비스의 collectFor를 호출한다.
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

    private boolean isCollectionTarget(Catalyst catalyst) {
        return catalyst != null && catalyst.getDeletedAt() == null && catalyst.getStatus() == CatalystStatus.ACTIVE;
    }
}
