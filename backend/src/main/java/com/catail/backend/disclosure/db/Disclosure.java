package com.catail.backend.disclosure.db;

import com.catail.backend.disclosure.domain.DisclosureProvider;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "disclosure",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_disclosure_provider_external_id",
                        columnNames = {"provider", "external_disclosure_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_disclosure_company_provider_received_date",
                        columnList = "company_id, provider, received_date DESC, external_disclosure_id DESC"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Disclosure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 30)
    private DisclosureProvider provider;

    @Column(name = "external_disclosure_id", nullable = false, length = 100)
    private String externalDisclosureId;

    @Column(name = "received_date", nullable = false)
    private LocalDate receivedDate;

    @Column(name = "report_name", nullable = false, length = 500)
    private String reportName;

    @Column(name = "submitter_name", nullable = false, length = 200)
    private String submitterName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static Disclosure create(
            Long companyId,
            DisclosureProvider provider,
            String externalDisclosureId,
            LocalDate receivedDate,
            String reportName,
            String submitterName
    ) {
        Disclosure disclosure = new Disclosure();
        disclosure.companyId = companyId;
        disclosure.provider = provider;
        disclosure.externalDisclosureId = externalDisclosureId;
        disclosure.receivedDate = receivedDate;
        disclosure.reportName = reportName;
        disclosure.submitterName = submitterName;
        return disclosure;
    }

    public void updateContent(String reportName, String submitterName) {
        this.reportName = reportName;
        this.submitterName = submitterName;
    }
}
