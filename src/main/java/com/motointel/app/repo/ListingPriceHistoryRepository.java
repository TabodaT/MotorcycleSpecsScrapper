package com.motointel.app.repo;

import com.motointel.app.domain.ListingPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ListingPriceHistoryRepository extends JpaRepository<ListingPriceHistory, Long> {
    List<ListingPriceHistory> findByListingIdOrderByObservedAtAsc(Long listingId);
    Optional<ListingPriceHistory> findTopByListingIdOrderByObservedAtDesc(Long listingId);
}
