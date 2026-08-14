package com.catail.backend.signal.application;

import com.catail.backend.signal.db.SignalRepository;
import com.catail.backend.signal.domain.NewsArticle;
import com.catail.backend.signal.outbound.naver.NaverNewsItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NewsArticleCollectionService {

    private final NewsArticleDeduplicator newsArticleDeduplicator;
    private final NewsArticleConverter newsArticleConverter;
    private final SignalRepository signalRepository;

    public List<NewsArticle> collect(Long catalystId, Map<String, List<NaverNewsItem>> resultsByQuery) {
        return newsArticleDeduplicator.deduplicate(resultsByQuery).stream()
                .filter(candidate -> !signalRepository.existsByCatalystIdAndLink(catalystId, candidate.normalizedLink()))
                .map(newsArticleConverter::convert)
                .toList();
    }
}
