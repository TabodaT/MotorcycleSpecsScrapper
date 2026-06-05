package com.motointel.app.repo;

import com.motointel.app.domain.CatalogSourceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogSourceSnapshotRepository extends JpaRepository<CatalogSourceSnapshot, Long> {
}
