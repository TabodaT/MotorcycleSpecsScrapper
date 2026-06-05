package com.motointel.app.repo;

import com.motointel.app.domain.MotorcycleModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MotorcycleModelRepository extends JpaRepository<MotorcycleModel, Long> {

    List<MotorcycleModel> findByManufacturerId(Long manufacturerId);

    Optional<MotorcycleModel> findByManufacturerIdAndNormalizedName(Long manufacturerId, String normalizedName);

    @Query("""
            SELECT m FROM MotorcycleModel m
            WHERE (:manufacturerId IS NULL OR m.manufacturerId = :manufacturerId)
              AND (:q IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR m.normalizedName LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY m.name ASC
            """)
    Page<MotorcycleModel> search(@Param("manufacturerId") Long manufacturerId,
                                 @Param("q") String q, Pageable pageable);

    @Query("""
            SELECT m FROM MotorcycleModel m
            WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :q, '%'))
               OR m.normalizedName LIKE LOWER(CONCAT('%', :q, '%'))
            ORDER BY m.name ASC
            """)
    List<MotorcycleModel> quickSearch(@Param("q") String q, Pageable pageable);

    long countByManufacturerId(Long manufacturerId);
}
