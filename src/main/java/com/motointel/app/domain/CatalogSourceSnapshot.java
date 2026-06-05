package com.motointel.app.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "catalog_source_snapshots")
public class CatalogSourceSnapshot extends BaseEntity {

    @Column(name = "page_id", nullable = false)
    private Long pageId;

    @Column(name = "storage_ref")
    private String storageRef;

    @Column(name = "fetched_at")
    private Instant fetchedAt;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "content_hash")
    private String contentHash;

    public Long getPageId() { return pageId; }
    public void setPageId(Long pageId) { this.pageId = pageId; }
    public String getStorageRef() { return storageRef; }
    public void setStorageRef(String storageRef) { this.storageRef = storageRef; }
    public Instant getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(Instant fetchedAt) { this.fetchedAt = fetchedAt; }
    public Integer getHttpStatus() { return httpStatus; }
    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }
    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }
}
