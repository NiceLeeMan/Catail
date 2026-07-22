package com.catail.backend.disclosure.db;

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
        name = "disclosure_remark",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_disclosure_remark_disclosure_code",
                        columnNames = {"disclosure_id", "code"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DisclosureRemark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "disclosure_id", nullable = false)
    private Long disclosureId;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static DisclosureRemark create(Long disclosureId, String code) {
        DisclosureRemark remark = new DisclosureRemark();
        remark.disclosureId = disclosureId;
        remark.code = code;
        return remark;
    }
}
