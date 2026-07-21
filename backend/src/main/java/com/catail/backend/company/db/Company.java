package com.catail.backend.company.db;

import com.catail.backend.company.domain.Market;
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
        name = "company",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_company_market_stock_code",
                        columnNames = {"market", "stock_code"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "market", nullable = false, length = 20)
    private Market market;

    @Column(name = "stock_code", nullable = false, length = 20)
    private String stockCode;

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @Column(name = "industry_name", length = 200)
    private String industryName;

    @Column(name = "logo_url", columnDefinition = "TEXT")
    private String logoUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static Company create(Market market, String stockCode, String companyName) {
        Company company = new Company();
        company.market = market;
        company.stockCode = stockCode;
        company.companyName = companyName;
        return company;
    }

    public void updateCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
