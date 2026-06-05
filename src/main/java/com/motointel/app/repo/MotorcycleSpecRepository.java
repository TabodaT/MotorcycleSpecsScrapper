package com.motointel.app.repo;

import com.motointel.app.domain.MotorcycleSpec;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MotorcycleSpecRepository extends JpaRepository<MotorcycleSpec, Long> {
    List<MotorcycleSpec> findByModelId(Long modelId);
}
