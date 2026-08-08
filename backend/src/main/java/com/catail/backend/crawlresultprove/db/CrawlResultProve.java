package com.catail.backend.crawlresultprove.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "crawl_result_prove",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_crawl_result_prove_result_version", columnNames = {"search_result_id", "version"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CrawlResultProve {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "search_result_id", nullable = false)
    private Long searchResultId;

    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static CrawlResultProve create(Long searchResultId, int version, String content) {
        CrawlResultProve prove = new CrawlResultProve();
        prove.searchResultId = searchResultId;
        prove.version = version;
        prove.content = content;
        return prove;
    }
}
