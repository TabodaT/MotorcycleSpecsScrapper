package com.motointel.app.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "motorcycle_models")
public class MotorcycleModel extends BaseEntity {

    @Column(name = "manufacturer_id", nullable = false)
    private Long manufacturerId;

    @Column(nullable = false)
    private String name;

    @Column(name = "normalized_name", nullable = false)
    private String normalizedName;

    @Column(name = "production_start_year")
    private Integer productionStartYear;

    @Column(name = "production_end_year")
    private Integer productionEndYear;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public Long getManufacturerId() { return manufacturerId; }
    public void setManufacturerId(Long manufacturerId) { this.manufacturerId = manufacturerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNormalizedName() { return normalizedName; }
    public void setNormalizedName(String normalizedName) { this.normalizedName = normalizedName; }
    public Integer getProductionStartYear() { return productionStartYear; }
    public void setProductionStartYear(Integer y) { this.productionStartYear = y; }
    public Integer getProductionEndYear() { return productionEndYear; }
    public void setProductionEndYear(Integer y) { this.productionEndYear = y; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
