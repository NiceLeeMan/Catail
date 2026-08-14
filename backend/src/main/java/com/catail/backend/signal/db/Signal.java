package com.catail.backend.signal.db;

import com.catail.backend.signal.domain.SignalStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Getter
@Entity
@Table(name = "signal")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Signal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "catalyst_id", nullable = false)
    private Long catalystId;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "originallink", columnDefinition = "TEXT")
    private String originallink;

    @Column(name = "link", nullable = false, columnDefinition = "TEXT")
    private String link;

    @Column(name = "pub_date", nullable = false)
    private OffsetDateTime pubDate;

    @Column(name = "press", length = 200)
    private String press;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SignalStatus status;

    @Column(name = "relevance_reason", columnDefinition = "TEXT")
    private String relevanceReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static Signal create(
            Long catalystId,
            String title,
            String description,
            String originallink,
            String link,
            OffsetDateTime pubDate,
            String press
    ) {
        Signal signal = new Signal();
        signal.catalystId = catalystId;
        signal.title = title;
        signal.description = description;
        signal.originallink = originallink;
        signal.link = link;
        signal.pubDate = pubDate;
        signal.press = press;
        signal.status = SignalStatus.PENDING;
        return signal;
    }

    public void changeStatus(SignalStatus status) {
        this.status = status;
    }
}
