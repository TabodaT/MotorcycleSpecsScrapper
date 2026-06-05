package com.motointel.app.repo;

import com.motointel.app.domain.MarketListingImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MarketListingImageRepository extends JpaRepository<MarketListingImage, Long> {
    List<MarketListingImage> findByListingId(Long listingId);
}
