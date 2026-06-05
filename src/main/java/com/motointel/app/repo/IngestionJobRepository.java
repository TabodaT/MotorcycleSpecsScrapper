package com.motointel.app.repo;

import com.motointel.app.domain.IngestionJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IngestionJobRepository extends JpaRepository<IngestionJob, Long> {

    @Query("""
            SELECT j FROM IngestionJob j
            WHERE (:type IS NULL OR j.type = :type)
              AND (:status IS NULL OR j.status = :status)
            ORDER BY j.startedAt DESC NULLS LAST, j.id DESC
            """)
    Page<IngestionJob> search(@Param("type") String type, @Param("status") String status, Pageable pageable);

    List<IngestionJob> findTop5ByOrderByStartedAtDescIdDesc();
}
