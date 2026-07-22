package com.catail.backend.disclosure.db;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DisclosureRemarkRepository extends JpaRepository<DisclosureRemark, Long> {

    List<DisclosureRemark> findByDisclosureIdIn(List<Long> disclosureIds);

    boolean existsByDisclosureIdAndCode(Long disclosureId, String code);
}
