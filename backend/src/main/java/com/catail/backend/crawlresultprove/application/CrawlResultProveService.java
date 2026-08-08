package com.catail.backend.crawlresultprove.application;

import com.catail.backend.crawlresultprove.db.CrawlResultProve;
import com.catail.backend.crawlresultprove.db.CrawlResultProveRepository;
import com.catail.backend.global.BusinessException;
import com.catail.backend.websearch.db.SearchResult;
import com.catail.backend.websearch.db.SearchResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CrawlResultProveService {

    private final CrawlResultProveRepository crawlResultProveRepository;
    private final SearchResultRepository searchResultRepository;
    private final ContentImprover contentImprover;

    @Transactional
    public CrawlResultProve advance(Long searchResultId) {
        return crawlResultProveRepository.findTopBySearchResultIdOrderByVersionDesc(searchResultId)
                .map(previous -> {
                    String improved = contentImprover.improve(previous.getContent());
                    return crawlResultProveRepository.save(
                            CrawlResultProve.create(searchResultId, previous.getVersion() + 1, improved));
                })
                .orElseGet(() -> seedFirstVersion(searchResultId));
    }

    @Transactional(readOnly = true)
    public List<CrawlResultProve> getHistory(Long searchResultId) {
        return crawlResultProveRepository.findBySearchResultIdOrderByVersionAsc(searchResultId);
    }

    private CrawlResultProve seedFirstVersion(Long searchResultId) {
        SearchResult searchResult = searchResultRepository.findById(searchResultId)
                .orElseThrow(() -> new BusinessException(CrawlResultProveErrorCode.SEARCH_RESULT_NOT_FOUND));
        if (searchResult.getContent() == null) {
            throw new BusinessException(CrawlResultProveErrorCode.CONTENT_NOT_CRAWLED);
        }
        return crawlResultProveRepository.save(CrawlResultProve.create(searchResultId, 1, searchResult.getContent()));
    }
}
