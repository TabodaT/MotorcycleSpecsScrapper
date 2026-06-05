package com.motointel.app.repo;

import com.motointel.app.domain.ModelAlias;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModelAliasRepository extends JpaRepository<ModelAlias, Long> {
    List<ModelAlias> findByModelId(Long modelId);
    boolean existsByModelIdAndNormalizedAlias(Long modelId, String normalizedAlias);
}
