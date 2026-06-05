package com.motointel.app.repo;

import com.motointel.app.domain.CatalogSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatalogSourceRepository extends JpaRepository<CatalogSource, Long> {
    List<CatalogSource> findByEnabledTrue();
}
