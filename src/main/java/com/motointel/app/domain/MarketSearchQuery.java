package com.motointel.app.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "market_search_queries")
public class MarketSearchQuery extends BaseEntity {

    @Column(name = "market_source_id", nullable = false)
    private Long marketSourceId;

    @Column(nullable = false)
    private String query;

    @Column(nullable = false)
    private boolean enabled;

    public Long getMarketSourceId() { return marketSourceId; }
    public void setMarketSourceId(Long marketSourceId) { this.marketSourceId = marketSourceId; }
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
