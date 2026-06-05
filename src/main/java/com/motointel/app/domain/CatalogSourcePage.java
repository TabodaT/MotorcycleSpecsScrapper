package com.motointel.app.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "catalog_source_pages")
public class CatalogSourcePage extends BaseEntity {

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Column(nullable = false)
    private String url;

    @Column(name = "page_type")
    private String pageType;

    @Column(name = "discovered_at")
    private Instant discoveredAt;

    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getPageType() { return pageType; }
    public void setPageType(String pageType) { this.pageType = pageType; }
    public Instant getDiscoveredAt() { return discoveredAt; }
    public void setDiscoveredAt(Instant discoveredAt) { this.discoveredAt = discoveredAt; }
}
