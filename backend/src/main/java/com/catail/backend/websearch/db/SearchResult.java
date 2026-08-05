package com.catail.backend.websearch.db;

import com.catail.backend.websearch.domain.CrawlFailureCode;
import com.catail.backend.websearch.domain.CrawlStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "search_result",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_search_result_execution_crawl_url", columnNames = {"search_execution_id", "crawl_url"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "search_execution_id", nullable = false)
    private Long searchExecutionId;

    @Column(name = "title", columnDefinition = "TEXT")
    private String title;

    @Column(name = "crawl_url", nullable = false, columnDefinition = "TEXT")
    private String crawlUrl;

    // ddl-auto=update 환경에서 기존 행에도 안전하게 적용되도록 컬럼 DEFAULT를 DDL에 직접 명시한다.
    @Enumerated(EnumType.STRING)
    @Column(name = "crawl_status", nullable = false, length = 20, columnDefinition = "VARCHAR(20) NOT NULL DEFAULT 'PENDING'")
    private CrawlStatus crawlStatus;

    @Column(name = "crawled_title", columnDefinition = "TEXT")
    private String crawledTitle;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "token_count")
    private Integer tokenCount;

    @Column(name = "warning_message", columnDefinition = "TEXT")
    private String warningMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "failure_code", length = 30)
    private CrawlFailureCode failureCode;

    @Column(name = "failure_detail", columnDefinition = "TEXT")
    private String failureDetail;

    @Column(name = "crawl_completed_at")
    private LocalDateTime crawlCompletedAt;

    // 문서 스키마 표에는 없지만 일시적 오류 재시도 상한(섹션 8)을 구현하려면 필요해 추가.
    @Column(name = "retry_count", nullable = false, columnDefinition = "INTEGER NOT NULL DEFAULT 0")
    private int retryCount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static SearchResult create(Long searchExecutionId, String title, String crawlUrl) {
        SearchResult result = new SearchResult();
        result.searchExecutionId = searchExecutionId;
        result.title = title;
        result.crawlUrl = crawlUrl;
        result.crawlStatus = CrawlStatus.PENDING;
        result.retryCount = 0;
        return result;
    }

    public void markCrawlProcessing() {
        this.crawlStatus = CrawlStatus.PROCESSING;
    }

    public void markCrawlSuccess(
            String crawledTitle, String content, LocalDateTime publishedAt, Integer tokenCount, String warningMessage) {
        this.crawlStatus = CrawlStatus.SUCCESS;
        this.crawledTitle = crawledTitle;
        this.content = content;
        this.publishedAt = publishedAt;
        this.tokenCount = tokenCount;
        this.warningMessage = warningMessage;
        this.crawlCompletedAt = LocalDateTime.now();
    }

    public void markCrawlFailed(CrawlFailureCode failureCode, String failureDetail) {
        this.crawlStatus = CrawlStatus.FAILED;
        this.failureCode = failureCode;
        this.failureDetail = failureDetail;
        this.crawlCompletedAt = LocalDateTime.now();
    }

    public void markCrawlRetryPending(String failureDetail) {
        this.crawlStatus = CrawlStatus.PENDING;
        this.retryCount++;
        this.failureDetail = failureDetail;
    }

    public void resetToPendingForRecovery() {
        this.crawlStatus = CrawlStatus.PENDING;
    }
}
