package com.catail.backend.crawlresultprove.db;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CrawlResultProveRepository extends JpaRepository<CrawlResultProve, Long> {

    Optional<CrawlResultProve> findTopBySearchResultIdOrderByVersionDesc(Long searchResultId);

    List<CrawlResultProve> findBySearchResultIdOrderByVersionAsc(Long searchResultId);
}
