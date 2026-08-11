package com.catail.backend.catalyst.db;

import com.catail.backend.catalyst.domain.CatalystCategory;
import com.catail.backend.catalyst.domain.CatalystStatus;
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
        name = "catalyst",
        indexes = {
                @Index(
                        name = "idx_catalysts_user_company",
                        columnList = "user_id, company_id"
                ),
                @Index(
                        name = "idx_catalysts_user_company_category",
                        columnList = "user_id, company_id, category"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Catalyst {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private CatalystCategory category;

    @Column(name = "detail", nullable = false, length = 300)
    private String detail;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CatalystStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static Catalyst create(
            Long userId,
            Long companyId,
            CatalystCategory category,
            String detail,
            String title,
            CatalystStatus status
    ) {
        Catalyst catalyst = new Catalyst();
        catalyst.userId = userId;
        catalyst.companyId = companyId;
        catalyst.category = category;
        catalyst.detail = detail;
        catalyst.title = title;
        catalyst.status = status;
        return catalyst;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}
