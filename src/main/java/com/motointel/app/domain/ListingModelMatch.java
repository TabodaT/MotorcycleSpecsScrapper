package com.motointel.app.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "listing_model_matches")
public class ListingModelMatch extends BaseEntity {

    @Column(name = "listing_id", nullable = false)
    private Long listingId;

    @Column(name = "model_id")
    private Long modelId;

    @Column(name = "variant_id")
    private Long variantId;

    @Column(nullable = false)
    private String status;

    private BigDecimal confidence;
    private String explanation;

    @Column(name = "is_manual", nullable = false)
    private boolean manual;

    @Column(name = "decided_at")
    private Instant decidedAt;

    public Long getListingId() { return listingId; }
    public void setListingId(Long listingId) { this.listingId = listingId; }
    public Long getModelId() { return modelId; }
    public void setModelId(Long modelId) { this.modelId = modelId; }
    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public boolean isManual() { return manual; }
    public void setManual(boolean manual) { this.manual = manual; }
    public Instant getDecidedAt() { return decidedAt; }
    public void setDecidedAt(Instant decidedAt) { this.decidedAt = decidedAt; }
}
