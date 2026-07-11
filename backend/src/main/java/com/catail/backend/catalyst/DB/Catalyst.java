package com.catail.backend.catalyst.DB;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "catalyst")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Catalyst {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Setter
    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Setter
    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CatalystStatus status;

    @Setter
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "search_conditions", nullable = false, columnDefinition = "jsonb")
    private List<String> searchConditions = new ArrayList<>();

    @Setter
    @ColumnDefault("4")
    @Column(name = "search_interval_hours", nullable = false)
    private Integer searchIntervalHours;

    @Column(name = "last_searched_at")
    private LocalDateTime lastSearchedAt;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}