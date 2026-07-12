package com.catail.backend.catalyst.DB;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatalystIndustryRepository extends JpaRepository<CatalystIndustry, Long> {

    List<CatalystIndustry> findByCatalystId(Long catalystId);

    List<CatalystIndustry> findByCatalystIdIn(List<Long> catalystIds);

    void deleteByCatalystId(Long catalystId);
}
