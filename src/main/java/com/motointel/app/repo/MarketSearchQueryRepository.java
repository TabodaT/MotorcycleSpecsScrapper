package com.motointel.app.repo;

import com.motointel.app.domain.MarketSearchQuery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MarketSearchQueryRepository extends JpaRepository<MarketSearchQuery, Long> {
    List<MarketSearchQuery> findByMarketSourceId(Long marketSourceId);
}
