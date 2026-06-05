package com.motointel.app.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "listing_price_history")
public class ListingPriceHistory extends BaseEntity {

    @Column(name = "listing_id", nullable = false)
    private Long listingId;

    @Column(nullable = false)
    private BigDecimal price;

    private String currency;

    @Column(name = "observed_at")
    private Instant observedAt;

    public Long getListingId() { return listingId; }
    public void setListingId(Long listingId) { this.listingId = listingId; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Instant getObservedAt() { return observedAt; }
    public void setObservedAt(Instant observedAt) { this.observedAt = observedAt; }
}
