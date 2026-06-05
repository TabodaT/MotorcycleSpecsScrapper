package com.motointel.app.dto;

/** Image reference with a ready-to-use URL the SPA can render directly. */
public record ImageDto(Long id, String storageRef, String sourceUrl, String url) {
}
