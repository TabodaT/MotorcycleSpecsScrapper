package com.motointel.app.repo;

import com.motointel.app.domain.MarketSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MarketSourceRepository extends JpaRepository<MarketSource, Long> {
    Optional<MarketSource> findByName(String name);
    List<MarketSource> findByEnabledTrue();
}
