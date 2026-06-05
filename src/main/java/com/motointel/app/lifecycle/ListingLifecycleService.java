package com.motointel.app.lifecycle;

import com.motointel.app.config.AppProperties;
import com.motointel.app.domain.ListingStatus;
import com.motointel.app.domain.ListingStatusHistory;
import com.motointel.app.domain.MarketListing;
import com.motointel.app.repo.ListingStatusHistoryRepository;
import com.motointel.app.repo.MarketListingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

/**
 * Drives listing statuses (§8) from observation events and the missing-scan counter,
 * using LISTING_REMOVED_THRESHOLD_N / LISTING_LIKELY_SOLD_DAYS_X from configuration.
 */
@Service
public class ListingLifecycleService {

    private final MarketListingRepository listingRepo;
    private final ListingStatusHistoryRepository statusHistoryRepo;
    private final AppProperties props;

    public ListingLifecycleService(MarketListingRepository listingRepo,
                                   ListingStatusHistoryRepository statusHistoryRepo,
                                   AppProperties props) {
        this.listingRepo = listingRepo;
        this.statusHistoryRepo = statusHistoryRepo;
        this.props = props;
    }

    /** Mark a listing as seen this scan/import: back to active, missing counter reset. */
    public void applyObserved(MarketListing listing, Instant observedAt) {
        if (ListingStatus.IGNORED.equals(listing.getStatus())) {
            return; // ignored is user-controlled only
        }
        listing.setConsecutiveMissingCount(0);
        if (observedAt != null) {
            if (listing.getLastObservedAt() == null || observedAt.isAfter(listing.getLastObservedAt())) {
                listing.setLastObservedAt(observedAt);
            }
        }
        transitionTo(listing, ListingStatus.ACTIVE, observedAt);
    }

    /** Apply a single missed scan, computing missing_once / removed / likely_sold. */
    @Transactional
    public void applyMissing(MarketListing listing) {
        if (ListingStatus.IGNORED.equals(listing.getStatus())) {
            return;
        }
        int n = props.getListing().getRemovedThresholdN();
        int x = props.getListing().getLikelySoldDaysX();
        int count = listing.getConsecutiveMissingCount() + 1;
        listing.setConsecutiveMissingCount(count);

        String newStatus;
        if (count >= n) {
            newStatus = wasActiveAtLeast(listing, x) ? ListingStatus.LIKELY_SOLD : ListingStatus.REMOVED;
        } else {
            newStatus = ListingStatus.MISSING_ONCE;
        }
        transitionTo(listing, newStatus, Instant.now());
        listingRepo.save(listing);
    }

    private boolean wasActiveAtLeast(MarketListing listing, int days) {
        if (listing.getFirstObservedAt() == null || listing.getLastObservedAt() == null) {
            return false;
        }
        return Duration.between(listing.getFirstObservedAt(), listing.getLastObservedAt())
                .toDays() >= days;
    }

    /** Set status and append a history row only if the status actually changed. */
    public void transitionTo(MarketListing listing, String newStatus, Instant at) {
        if (newStatus.equals(listing.getStatus())) {
            return;
        }
        listing.setStatus(newStatus);
        ListingStatusHistory h = new ListingStatusHistory();
        h.setListingId(listing.getId());
        h.setStatus(newStatus);
        h.setAt(at == null ? Instant.now() : at);
        statusHistoryRepo.save(h);
    }
}
