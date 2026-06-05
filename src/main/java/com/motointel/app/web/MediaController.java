package com.motointel.app.web;

import com.motointel.app.storage.StorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/** Serves stored media (catalog/listing images, snapshots) by storage ref. */
@RestController
public class MediaController {

    private static final String PREFIX = "/api/media/";

    private final StorageService storage;

    public MediaController(StorageService storage) {
        this.storage = storage;
    }

    @GetMapping("/api/media/**")
    public ResponseEntity<Resource> media(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (!uri.startsWith(PREFIX)) {
            return ResponseEntity.notFound().build();
        }
        String ref = URLDecoder.decode(uri.substring(PREFIX.length()), StandardCharsets.UTF_8);
        Resource resource = storage.resolve(ref);
        if (resource == null || !resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().contentType(contentType(ref)).body(resource);
    }

    private MediaType contentType(String ref) {
        String lower = ref.toLowerCase();
        if (lower.endsWith(".svg")) return MediaType.valueOf("image/svg+xml");
        if (lower.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
        if (lower.endsWith(".gif")) return MediaType.IMAGE_GIF;
        if (lower.endsWith(".webp")) return MediaType.valueOf("image/webp");
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return MediaType.TEXT_HTML;
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
