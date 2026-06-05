package com.motointel.app.repo;

import com.motointel.app.domain.CatalogSourcePage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CatalogSourcePageRepository extends JpaRepository<CatalogSourcePage, Long> {
    Optional<CatalogSourcePage> findByUrl(String url);
}
