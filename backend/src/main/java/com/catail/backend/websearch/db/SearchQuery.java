package com.catail.backend.websearch.db;

import com.catail.backend.searchplan.domain.Criterion;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "search_query",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_search_query_execution_text", columnNames = {"search_execution_id", "query_text"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchQuery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "search_execution_id", nullable = false)
    private Long searchExecutionId;

    @Column(name = "search_batch_id")
    private Long searchBatchId;

    @Enumerated(EnumType.STRING)
    @Column(name = "criterion_code", nullable = false, length = 50)
    private Criterion criterionCode;

    @Column(name = "query_text", nullable = false, columnDefinition = "TEXT")
    private String queryText;

    @Column(name = "language", nullable = false, length = 10)
    private String language;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static SearchQuery create(Long searchExecutionId, Criterion criterionCode, String queryText, String language) {
        SearchQuery query = new SearchQuery();
        query.searchExecutionId = searchExecutionId;
        query.criterionCode = criterionCode;
        query.queryText = queryText;
        query.language = language;
        return query;
    }

    public void assignBatch(Long searchBatchId) {
        this.searchBatchId = searchBatchId;
    }
}
