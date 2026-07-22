package com.catail.backend.disclosure.db;

import com.catail.backend.disclosure.domain.DisclosureProvider;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DisclosureRepository extends JpaRepository<Disclosure, Long> {

    Optional<Disclosure> findByProviderAndExternalDisclosureId(DisclosureProvider provider, String externalDisclosureId);

    @Query("SELECT d FROM Disclosure d "
            + "WHERE d.companyId = :companyId AND d.provider = :provider "
            + "ORDER BY d.receivedDate DESC, d.externalDisclosureId DESC")
    List<Disclosure> findFirstPage(
            @Param("companyId") Long companyId,
            @Param("provider") DisclosureProvider provider,
            Pageable pageable);

    @Query("SELECT d FROM Disclosure d "
            + "WHERE d.companyId = :companyId AND d.provider = :provider "
            + "AND (d.receivedDate < :cursorDate "
            + "     OR (d.receivedDate = :cursorDate AND d.externalDisclosureId < :cursorExternalId)) "
            + "ORDER BY d.receivedDate DESC, d.externalDisclosureId DESC")
    List<Disclosure> findNextPage(
            @Param("companyId") Long companyId,
            @Param("provider") DisclosureProvider provider,
            @Param("cursorDate") LocalDate cursorDate,
            @Param("cursorExternalId") String cursorExternalId,
            Pageable pageable);
}
