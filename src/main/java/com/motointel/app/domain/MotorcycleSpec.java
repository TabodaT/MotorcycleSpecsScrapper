package com.motointel.app.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "motorcycle_specs")
public class MotorcycleSpec extends BaseEntity {

    @Column(name = "model_id", nullable = false)
    private Long modelId;

    @Column(name = "variant_id")
    private Long variantId;

    @Column(name = "engine_capacity_cc")
    private BigDecimal engineCapacityCc;

    @Column(name = "power_kw")
    private BigDecimal powerKw;

    @Column(name = "torque_nm")
    private BigDecimal torqueNm;

    @Column(name = "dry_weight_kg")
    private BigDecimal dryWeightKg;

    @Column(name = "wet_weight_kg")
    private BigDecimal wetWeightKg;

    @Column(name = "seat_height_mm")
    private BigDecimal seatHeightMm;

    @Column(name = "fuel_capacity_l")
    private BigDecimal fuelCapacityL;

    @Column(name = "top_speed_kmh")
    private BigDecimal topSpeedKmh;

    private String cooling;
    private String transmission;

    @Column(name = "final_drive")
    private String finalDrive;

    private Boolean abs;

    @Column(name = "is_electric", nullable = false)
    private boolean isElectric;

    public Long getModelId() { return modelId; }
    public void setModelId(Long modelId) { this.modelId = modelId; }
    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }
    public BigDecimal getEngineCapacityCc() { return engineCapacityCc; }
    public void setEngineCapacityCc(BigDecimal v) { this.engineCapacityCc = v; }
    public BigDecimal getPowerKw() { return powerKw; }
    public void setPowerKw(BigDecimal v) { this.powerKw = v; }
    public BigDecimal getTorqueNm() { return torqueNm; }
    public void setTorqueNm(BigDecimal v) { this.torqueNm = v; }
    public BigDecimal getDryWeightKg() { return dryWeightKg; }
    public void setDryWeightKg(BigDecimal v) { this.dryWeightKg = v; }
    public BigDecimal getWetWeightKg() { return wetWeightKg; }
    public void setWetWeightKg(BigDecimal v) { this.wetWeightKg = v; }
    public BigDecimal getSeatHeightMm() { return seatHeightMm; }
    public void setSeatHeightMm(BigDecimal v) { this.seatHeightMm = v; }
    public BigDecimal getFuelCapacityL() { return fuelCapacityL; }
    public void setFuelCapacityL(BigDecimal v) { this.fuelCapacityL = v; }
    public BigDecimal getTopSpeedKmh() { return topSpeedKmh; }
    public void setTopSpeedKmh(BigDecimal v) { this.topSpeedKmh = v; }
    public String getCooling() { return cooling; }
    public void setCooling(String cooling) { this.cooling = cooling; }
    public String getTransmission() { return transmission; }
    public void setTransmission(String transmission) { this.transmission = transmission; }
    public String getFinalDrive() { return finalDrive; }
    public void setFinalDrive(String finalDrive) { this.finalDrive = finalDrive; }
    public Boolean getAbs() { return abs; }
    public void setAbs(Boolean abs) { this.abs = abs; }
    public boolean isElectric() { return isElectric; }
    public void setElectric(boolean electric) { isElectric = electric; }
}
