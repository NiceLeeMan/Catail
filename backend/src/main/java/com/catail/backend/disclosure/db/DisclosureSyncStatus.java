package com.catail.backend.disclosure.db;

import com.catail.backend.disclosure.domain.DisclosureProvider;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "disclosure_sync_status",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_disclosure_sync_status_company_provider",
                        columnNames = {"company_id", "provider"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DisclosureSyncStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 30)
    private DisclosureProvider provider;

    @Column(name = "initial_sync_completed_at")
    private LocalDateTime initialSyncCompletedAt;

    @Column(name = "last_successful_sync_at")
    private LocalDateTime lastSuccessfulSyncAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static DisclosureSyncStatus create(Long companyId, DisclosureProvider provider) {
        DisclosureSyncStatus status = new DisclosureSyncStatus();
        status.companyId = companyId;
        status.provider = provider;
        return status;
    }

    public boolean isInitialSyncCompleted() {
        return initialSyncCompletedAt != null;
    }

    public void markSuccess() {
        LocalDateTime now = LocalDateTime.now();
        if (initialSyncCompletedAt == null) {
            initialSyncCompletedAt = now;
        }
        lastSuccessfulSyncAt = now;
    }
}
