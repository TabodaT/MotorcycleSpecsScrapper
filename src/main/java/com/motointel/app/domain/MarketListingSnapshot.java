package com.motointel.app.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "market_listing_snapshots")
public class MarketListingSnapshot extends BaseEntity {

    @Column(name = "listing_id", nullable = false)
    private Long listingId;

    @Column(name = "storage_ref")
    private String storageRef;

    @Column(name = "captured_at")
    private Instant capturedAt;

    @Column(name = "content_hash")
    private String contentHash;

    public Long getListingId() { return listingId; }
    public void setListingId(Long listingId) { this.listingId = listingId; }
    public String getStorageRef() { return storageRef; }
    public void setStorageRef(String storageRef) { this.storageRef = storageRef; }
    public Instant getCapturedAt() { return capturedAt; }
    public void setCapturedAt(Instant capturedAt) { this.capturedAt = capturedAt; }
    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }
}
