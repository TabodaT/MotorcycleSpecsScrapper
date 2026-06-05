package com.motointel.app.dto;

/** Bare (unwrapped) health payload (§4 exception). */
public record HealthDto(String status, String db, String version) {
}
