package com.catail.backend.websearch.db;

import com.catail.backend.websearch.domain.SearchBatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SearchBatchRepository extends JpaRepository<SearchBatch, Long> {

    List<SearchBatch> findByStatusAndExternalJobIdIsNotNull(SearchBatchStatus status);

    List<SearchBatch> findBySearchExecutionId(Long searchExecutionId);
}
