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

    /**
     * {@code like} must be a pre-lowercased {@code %term%} pattern (or null for no text filter).
     * Binding it directly to LIKE lets Postgres infer the text type from the column, avoiding the
     * {@code lower(bytea)} type-inference error that a null param inside LOWER(CONCAT(...)) triggers.
     */
    @Query("""
            SELECT m FROM MotorcycleModel m
            WHERE (:manufacturerId IS NULL OR m.manufacturerId = :manufacturerId)
              AND (:like IS NULL OR LOWER(m.name) LIKE :like OR LOWER(m.normalizedName) LIKE :like)
            ORDER BY m.name ASC
            """)
    Page<MotorcycleModel> search(@Param("manufacturerId") Long manufacturerId,
                                 @Param("like") String like, Pageable pageable);

    @Query("""
            SELECT m FROM MotorcycleModel m
            WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :q, '%'))
               OR m.normalizedName LIKE LOWER(CONCAT('%', :q, '%'))
            ORDER BY m.name ASC
            """)
    List<MotorcycleModel> quickSearch(@Param("q") String q, Pageable pageable);

    long countByManufacturerId(Long manufacturerId);
}
