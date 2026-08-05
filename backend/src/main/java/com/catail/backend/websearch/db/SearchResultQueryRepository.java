package com.catail.backend.websearch.db;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchResultQueryRepository extends JpaRepository<SearchResultQuery, SearchResultQueryId> {
}
