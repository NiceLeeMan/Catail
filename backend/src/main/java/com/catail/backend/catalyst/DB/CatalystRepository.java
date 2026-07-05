package com.catail.backend.catalyst.DB;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CatalystRepository extends JpaRepository<Catalyst, Long> {

    Optional<Catalyst> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    Page<Catalyst> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);
}
