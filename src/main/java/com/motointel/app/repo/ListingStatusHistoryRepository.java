package com.motointel.app.repo;

import com.motointel.app.domain.ListingStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListingStatusHistoryRepository extends JpaRepository<ListingStatusHistory, Long> {
    List<ListingStatusHistory> findByListingIdOrderByAtAsc(Long listingId);
}
