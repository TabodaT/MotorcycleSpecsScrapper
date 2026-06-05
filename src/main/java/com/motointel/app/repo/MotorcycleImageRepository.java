package com.motointel.app.repo;

import com.motointel.app.domain.MotorcycleImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MotorcycleImageRepository extends JpaRepository<MotorcycleImage, Long> {
    List<MotorcycleImage> findByModelId(Long modelId);
}
