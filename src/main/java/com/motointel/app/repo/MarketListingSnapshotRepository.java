package com.motointel.app.repo;

import com.motointel.app.domain.MarketListingSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketListingSnapshotRepository extends JpaRepository<MarketListingSnapshot, Long> {
}
