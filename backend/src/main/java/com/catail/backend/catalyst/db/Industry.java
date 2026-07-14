package com.catail.backend.catalyst.db;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "industry",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_industry_name",
                        columnNames = "name"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Industry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static Industry create(String name) {
        Industry industry = new Industry();
        industry.name = name;
        return industry;
    }
}