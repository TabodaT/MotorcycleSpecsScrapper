package com.motointel.app.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "spec_evidence")
public class SpecEvidence extends BaseEntity {

    @Column(name = "spec_id", nullable = false)
    private Long specId;

    @Column(name = "snapshot_id")
    private Long snapshotId;

    @Column(name = "field_name")
    private String fieldName;

    @Column(name = "raw_value")
    private String rawValue;

    public Long getSpecId() { return specId; }
    public void setSpecId(Long specId) { this.specId = specId; }
    public Long getSnapshotId() { return snapshotId; }
    public void setSnapshotId(Long snapshotId) { this.snapshotId = snapshotId; }
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public String getRawValue() { return rawValue; }
    public void setRawValue(String rawValue) { this.rawValue = rawValue; }
}
