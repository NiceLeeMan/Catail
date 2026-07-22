package com.catail.backend.disclosure.db;

import com.catail.backend.disclosure.domain.DisclosureProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DisclosureSyncStatusRepository extends JpaRepository<DisclosureSyncStatus, Long> {

    Optional<DisclosureSyncStatus> findByCompanyIdAndProvider(Long companyId, DisclosureProvider provider);
}
