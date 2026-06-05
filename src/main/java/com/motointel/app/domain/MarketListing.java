package com.motointel.app.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "market_listings")
public class MarketListing extends BaseEntity {

    @Column(name = "market_source_id")
    private Long marketSourceId;

    @Column(nullable = false)
    private String source;

    @Column(name = "external_id")
    private String externalId;

    @Column(nullable = false)
    private String url;

    @Column(name = "normalized_url", nullable = false)
    private String normalizedUrl;

    @Column(nullable = false)
    private String title;

    private String description;
    private BigDecimal price;
    private String currency;
    private String location;

    @Column(name = "seller_type")
    private String sellerType;

    @Column(name = "posted_date")
    private LocalDate postedDate;

    @Column(name = "first_observed_at")
    private Instant firstObservedAt;

    @Column(name = "last_observed_at")
    private Instant lastObservedAt;

    @Column(nullable = false)
    private String status;

    @Column(name = "extracted_year")
    private Integer extractedYear;

    @Column(name = "extracted_manufacturer")
    private String extractedManufacturer;

    @Column(name = "extracted_model_text")
    private String extractedModelText;

    @Column(name = "extracted_capacity_cc")
    private BigDecimal extractedCapacityCc;

    @Column(name = "extracted_mileage_km")
    private Integer extractedMileageKm;

    @Column(name = "consecutive_missing_count", nullable = false)
    private int consecutiveMissingCount;

    @Column(name = "latest_snapshot_id")
    private Long latestSnapshotId;

    public Long getMarketSourceId() { return marketSourceId; }
    public void setMarketSourceId(Long marketSourceId) { this.marketSourceId = marketSourceId; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getNormalizedUrl() { return normalizedUrl; }
    public void setNormalizedUrl(String normalizedUrl) { this.normalizedUrl = normalizedUrl; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getSellerType() { return sellerType; }
    public void setSellerType(String sellerType) { this.sellerType = sellerType; }
    public LocalDate getPostedDate() { return postedDate; }
    public void setPostedDate(LocalDate postedDate) { this.postedDate = postedDate; }
    public Instant getFirstObservedAt() { return firstObservedAt; }
    public void setFirstObservedAt(Instant firstObservedAt) { this.firstObservedAt = firstObservedAt; }
    public Instant getLastObservedAt() { return lastObservedAt; }
    public void setLastObservedAt(Instant lastObservedAt) { this.lastObservedAt = lastObservedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getExtractedYear() { return extractedYear; }
    public void setExtractedYear(Integer extractedYear) { this.extractedYear = extractedYear; }
    public String getExtractedManufacturer() { return extractedManufacturer; }
    public void setExtractedManufacturer(String v) { this.extractedManufacturer = v; }
    public String getExtractedModelText() { return extractedModelText; }
    public void setExtractedModelText(String v) { this.extractedModelText = v; }
    public BigDecimal getExtractedCapacityCc() { return extractedCapacityCc; }
    public void setExtractedCapacityCc(BigDecimal v) { this.extractedCapacityCc = v; }
    public Integer getExtractedMileageKm() { return extractedMileageKm; }
    public void setExtractedMileageKm(Integer v) { this.extractedMileageKm = v; }
    public int getConsecutiveMissingCount() { return consecutiveMissingCount; }
    public void setConsecutiveMissingCount(int v) { this.consecutiveMissingCount = v; }
    public Long getLatestSnapshotId() { return latestSnapshotId; }
    public void setLatestSnapshotId(Long latestSnapshotId) { this.latestSnapshotId = latestSnapshotId; }
}
