package com.catail.backend.websearch.db;

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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static SearchResult create(Long searchExecutionId, String title, String crawlUrl) {
        SearchResult result = new SearchResult();
        result.searchExecutionId = searchExecutionId;
        result.title = title;
        result.crawlUrl = crawlUrl;
        return result;
    }
}
