package com.catail.backend.signal.db;

import com.catail.backend.signal.domain.SignalStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SignalRepository extends JpaRepository<Signal, Long> {

    boolean existsByCatalystIdAndLink(Long catalystId, String link);

    @Query("SELECT s FROM Signal s WHERE s.catalystId = :catalystId AND s.status = :status "
            + "ORDER BY s.createdAt DESC, s.id DESC")
    List<Signal> findFirstPage(
            @Param("catalystId") Long catalystId,
            @Param("status") SignalStatus status,
            Pageable pageable);

    @Query("SELECT s FROM Signal s WHERE s.catalystId = :catalystId AND s.status = :status "
            + "AND (s.createdAt < :cursorCreatedAt "
            + "     OR (s.createdAt = :cursorCreatedAt AND s.id < :cursorId)) "
            + "ORDER BY s.createdAt DESC, s.id DESC")
    List<Signal> findNextPage(
            @Param("catalystId") Long catalystId,
            @Param("status") SignalStatus status,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable);

    @Query("SELECT s FROM Signal s WHERE s.catalystId = :catalystId AND s.status = :status "
            + "ORDER BY s.pubDate DESC, s.id DESC")
    List<Signal> findTimeline(@Param("catalystId") Long catalystId, @Param("status") SignalStatus status);
}
