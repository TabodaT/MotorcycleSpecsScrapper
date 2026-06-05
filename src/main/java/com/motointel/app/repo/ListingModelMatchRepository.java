package com.motointel.app.repo;

import com.motointel.app.domain.ListingModelMatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ListingModelMatchRepository extends JpaRepository<ListingModelMatch, Long> {
    Optional<ListingModelMatch> findByListingId(Long listingId);
    long countByStatus(String status);
    Page<ListingModelMatch> findByStatusOrderByDecidedAtDesc(String status, Pageable pageable);
}
