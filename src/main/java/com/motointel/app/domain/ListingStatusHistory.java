package com.motointel.app.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "listing_status_history")
public class ListingStatusHistory extends BaseEntity {

    @Column(name = "listing_id", nullable = false)
    private Long listingId;

    @Column(nullable = false)
    private String status;

    @Column(name = "at")
    private Instant at;

    public Long getListingId() { return listingId; }
    public void setListingId(Long listingId) { this.listingId = listingId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getAt() { return at; }
    public void setAt(Instant at) { this.at = at; }
}
