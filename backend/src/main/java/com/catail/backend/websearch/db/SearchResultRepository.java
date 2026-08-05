package com.catail.backend.websearch.db;

import com.catail.backend.websearch.domain.CrawlStatus;
import com.catail.backend.websearch.domain.SearchExecutionStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SearchResultRepository extends JpaRepository<SearchResult, Long> {

    Optional<SearchResult> findBySearchExecutionIdAndCrawlUrl(Long searchExecutionId, String crawlUrl);

    List<SearchResult> findBySearchExecutionId(Long searchExecutionId);

    // 서버 재시작 복구 대상(PROCESSING으로 남은 행) 조회용.
    List<SearchResult> findByCrawlStatus(CrawlStatus crawlStatus);

    // SearchResult는 SearchExecution에 대한 연관관계(@ManyToOne) 없이 searchExecutionId만 갖고 있으므로,
    // 소속 실행이 SUCCESS/PARTIAL_FAILURE인지는 연관 탐색이 아니라 서브쿼리로 확인한다.
    @Query("""
            SELECT r FROM SearchResult r
            WHERE r.crawlStatus = :crawlStatus
              AND r.searchExecutionId IN (
                  SELECT e.id FROM SearchExecution e WHERE e.status IN :executionStatuses
              )
            ORDER BY r.createdAt ASC
            """)
    List<SearchResult> findCrawlCandidates(
            @Param("crawlStatus") CrawlStatus crawlStatus,
            @Param("executionStatuses") List<SearchExecutionStatus> executionStatuses,
            Pageable pageable);
}
