package com.motointel.app.common;

import org.springframework.data.domain.Page;

/** Pagination metadata for list responses (§4). */
public record PageMeta(long total, int page, int size) {

    public static PageMeta of(Page<?> page) {
        return new PageMeta(page.getTotalElements(), page.getNumber(), page.getSize());
    }
}
