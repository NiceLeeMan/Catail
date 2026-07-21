package com.catail.backend.company.db;

import com.catail.backend.company.domain.Market;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByMarketAndStockCode(Market market, String stockCode);

    Page<Company> findByMarket(Market market, Pageable pageable);

    @Query("SELECT c FROM Company c WHERE c.market = :market "
            + "AND (c.companyName LIKE CONCAT('%', :keyword, '%') OR c.stockCode LIKE CONCAT('%', :keyword, '%'))")
    Page<Company> searchByKeyword(@Param("market") Market market, @Param("keyword") String keyword, Pageable pageable);
}
