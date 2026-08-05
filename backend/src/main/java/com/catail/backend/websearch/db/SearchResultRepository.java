package com.catail.backend.websearch.db;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SearchResultRepository extends JpaRepository<SearchResult, Long> {

    Optional<SearchResult> findBySearchExecutionIdAndCrawlUrl(Long searchExecutionId, String crawlUrl);

    List<SearchResult> findBySearchExecutionId(Long searchExecutionId);
}
