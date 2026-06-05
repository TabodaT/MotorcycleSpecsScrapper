package com.motointel.app.repo;

import com.motointel.app.domain.IngestionJobError;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IngestionJobErrorRepository extends JpaRepository<IngestionJobError, Long> {
    List<IngestionJobError> findByJobIdOrderByIdAsc(Long jobId);
    List<IngestionJobError> findTop10ByOrderByCreatedAtDescIdDesc();
}
