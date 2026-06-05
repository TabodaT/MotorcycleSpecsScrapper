package com.motointel.app.repo;

import com.motointel.app.domain.MotorcycleVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface MotorcycleVariantRepository extends JpaRepository<MotorcycleVariant, Long> {
    List<MotorcycleVariant> findByModelId(Long modelId);
    List<MotorcycleVariant> findByModelIdIn(Collection<Long> modelIds);
}
