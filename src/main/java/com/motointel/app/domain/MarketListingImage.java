package com.motointel.app.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "market_listing_images")
public class MarketListingImage extends BaseEntity {

    @Column(name = "listing_id", nullable = false)
    private Long listingId;

    @Column(name = "storage_ref", nullable = false)
    private String storageRef;

    @Column(name = "source_url")
    private String sourceUrl;

    public Long getListingId() { return listingId; }
    public void setListingId(Long listingId) { this.listingId = listingId; }
    public String getStorageRef() { return storageRef; }
    public void setStorageRef(String storageRef) { this.storageRef = storageRef; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
}
