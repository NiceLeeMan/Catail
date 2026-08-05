package com.catail.backend.websearch.db;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SearchQueryRepository extends JpaRepository<SearchQuery, Long> {

    List<SearchQuery> findBySearchExecutionId(Long searchExecutionId);

    List<SearchQuery> findBySearchBatchId(Long searchBatchId);

    Optional<SearchQuery> findBySearchExecutionIdAndQueryText(Long searchExecutionId, String queryText);
}
