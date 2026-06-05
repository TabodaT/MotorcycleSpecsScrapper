package com.motointel.app.repo;

import com.motointel.app.domain.MarketListing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MarketListingRepository extends JpaRepository<MarketListing, Long> {

    Optional<MarketListing> findBySourceAndExternalId(String source, String externalId);

    Optional<MarketListing> findBySourceAndNormalizedUrl(String source, String normalizedUrl);

    long countByStatus(String status);

    List<MarketListing> findByStatus(String status);

    /**
     * Dynamic, paginated listing search. Nullable params are CAST so Postgres can infer
     * their type even when only used in an {@code IS NULL} comparison.
     */
    @Query(value = """
            SELECT l.* FROM market_listings l
            LEFT JOIN listing_model_matches m ON m.listing_id = l.id
            LEFT JOIN motorcycle_models mo ON mo.id = m.model_id
            WHERE (CAST(:source AS text) IS NULL OR l.source = :source)
              AND (CAST(:status AS text) IS NULL OR l.status = :status)
              AND (CAST(:matchStatus AS text) IS NULL OR m.status = :matchStatus)
              AND (CAST(:manufacturerId AS bigint) IS NULL OR mo.manufacturer_id = :manufacturerId)
              AND (CAST(:q AS text) IS NULL
                   OR l.title ILIKE '%' || :q || '%'
                   OR l.description ILIKE '%' || :q || '%')
            ORDER BY l.last_observed_at DESC NULLS LAST, l.id DESC
            """,
            countQuery = """
            SELECT count(DISTINCT l.id) FROM market_listings l
            LEFT JOIN listing_model_matches m ON m.listing_id = l.id
            LEFT JOIN motorcycle_models mo ON mo.id = m.model_id
            WHERE (CAST(:source AS text) IS NULL OR l.source = :source)
              AND (CAST(:status AS text) IS NULL OR l.status = :status)
              AND (CAST(:matchStatus AS text) IS NULL OR m.status = :matchStatus)
              AND (CAST(:manufacturerId AS bigint) IS NULL OR mo.manufacturer_id = :manufacturerId)
              AND (CAST(:q AS text) IS NULL
                   OR l.title ILIKE '%' || :q || '%'
                   OR l.description ILIKE '%' || :q || '%')
            """,
            nativeQuery = true)
    Page<MarketListing> search(@Param("source") String source,
                               @Param("status") String status,
                               @Param("matchStatus") String matchStatus,
                               @Param("manufacturerId") Long manufacturerId,
                               @Param("q") String q,
                               Pageable pageable);
}
