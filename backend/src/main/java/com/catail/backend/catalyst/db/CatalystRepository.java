package com.catail.backend.catalyst.db;

import com.catail.backend.catalyst.domain.CatalystCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CatalystRepository extends JpaRepository<Catalyst, Long> {

    @Query("SELECT COUNT(c) FROM Catalyst c WHERE c.userId = :userId AND c.companyId = :companyId AND c.deletedAt IS NULL")
    long countActive(@Param("userId") Long userId, @Param("companyId") Long companyId);

    @Query("SELECT COUNT(c) FROM Catalyst c WHERE c.userId = :userId AND c.companyId = :companyId "
            + "AND c.category = :category AND c.deletedAt IS NULL")
    long countActiveByCategory(@Param("userId") Long userId, @Param("companyId") Long companyId,
                                @Param("category") CatalystCategory category);
}
