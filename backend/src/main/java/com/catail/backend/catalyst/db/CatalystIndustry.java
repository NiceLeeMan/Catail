package com.catail.backend.catalyst.db;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@Table(
        name = "catalyst_industry",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_catalyst_industry",
                        columnNames = {"catalyst_id", "industry_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CatalystIndustry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "catalyst_id", nullable = false)
    private Long catalystId;

    @Setter
    @Column(name = "industry_id", nullable = false)
    private Long industryId;
}