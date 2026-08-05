package com.catail.backend.websearch.db;

import com.catail.backend.websearch.domain.SearchBatchStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "search_batch",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_search_batch_external_job_id", columnNames = "external_job_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "search_execution_id", nullable = false)
    private Long searchExecutionId;

    @Column(name = "external_job_id", length = 255)
    private String externalJobId;

    @Column(name = "results_location", columnDefinition = "TEXT")
    private String resultsLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SearchBatchStatus status;

    @Column(name = "poll_count", nullable = false)
    private int pollCount;

    @Column(name = "last_polled_at")
    private LocalDateTime lastPolledAt;

    // 문서 표(7.2)에는 없지만 실패 사유를 실제로 저장하려면 필요해 추가.
    @Column(name = "failure_reason", length = 50)
    private String failureReason;

    // 문서 표(7.2)에는 없지만 429 Retry-After를 다음 폴링 최소 시각으로 반영하려면 필요해 추가.
    @Column(name = "next_poll_at")
    private LocalDateTime nextPollAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static SearchBatch create(Long searchExecutionId) {
        SearchBatch batch = new SearchBatch();
        batch.searchExecutionId = searchExecutionId;
        batch.status = SearchBatchStatus.PENDING;
        batch.pollCount = 0;
        return batch;
    }

    public void markSubmitted(String externalJobId, String resultsLocation) {
        this.externalJobId = externalJobId;
        this.resultsLocation = resultsLocation;
    }

    public void markPolled() {
        this.pollCount++;
        this.lastPolledAt = LocalDateTime.now();
        this.nextPollAt = null;
    }

    public void markPolledWithRetryAfter(int retryAfterSeconds) {
        this.pollCount++;
        LocalDateTime now = LocalDateTime.now();
        this.lastPolledAt = now;
        this.nextPollAt = now.plusSeconds(retryAfterSeconds);
    }

    public void markSuccess() {
        this.status = SearchBatchStatus.SUCCESS;
        this.completedAt = LocalDateTime.now();
    }

    public void markFailed(String failureReason) {
        this.status = SearchBatchStatus.FAILED;
        this.failureReason = failureReason;
        this.completedAt = LocalDateTime.now();
    }

    public boolean isDueForPolling(LocalDateTime now, long pollIntervalSeconds) {
        if (nextPollAt != null) {
            return !now.isBefore(nextPollAt);
        }
        return lastPolledAt == null || lastPolledAt.plusSeconds(pollIntervalSeconds).isBefore(now)
                || lastPolledAt.plusSeconds(pollIntervalSeconds).isEqual(now);
    }
}
