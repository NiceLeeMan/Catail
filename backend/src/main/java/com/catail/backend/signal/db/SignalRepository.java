package com.catail.backend.signal.db;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SignalRepository extends JpaRepository<Signal, Long> {

    boolean existsByCatalystIdAndLink(Long catalystId, String link);
}
