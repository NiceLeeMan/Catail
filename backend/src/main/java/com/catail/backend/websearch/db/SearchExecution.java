package com.catail.backend.websearch.db;

import com.catail.backend.websearch.domain.SearchExecutionStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "search_execution")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    // 문서 스펙(VARCHAR(50))은 자유 서술형 분석 범위 원문을 담기엔 너무 좁아 report_name과 동일하게 500자로 확장.
    @Column(name = "analysis_scope", nullable = false, length = 500)
    private String analysisScope;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SearchExecutionStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public static SearchExecution create(Long userId, Long companyId, String analysisScope) {
        SearchExecution execution = new SearchExecution();
        execution.userId = userId;
        execution.companyId = companyId;
        execution.analysisScope = analysisScope;
        execution.status = SearchExecutionStatus.PENDING;
        return execution;
    }

    public void markSuccess() {
        this.status = SearchExecutionStatus.SUCCESS;
        this.completedAt = LocalDateTime.now();
    }

    public void markPartialFailure() {
        this.status = SearchExecutionStatus.PARTIAL_FAILURE;
        this.completedAt = LocalDateTime.now();
    }

    public void markFailure() {
        this.status = SearchExecutionStatus.FAILURE;
        this.completedAt = LocalDateTime.now();
    }
}
