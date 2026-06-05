package com.motointel.app.storage;

import com.motointel.app.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Durable storage abstraction. Snapshots/media are written under {@code STORAGE_ROOT}.
 * Resolution falls back to the classpath so seed media renders on first boot even though
 * the named storage volume starts empty (§6).
 */
@Service
public class StorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    private final Path root;

    public StorageService(AppProperties props) {
        this.root = Path.of(props.getStorage().getRoot()).toAbsolutePath().normalize();
    }

    /** Persist text content (e.g. a raw HTML snapshot) under the given relative ref. */
    public String saveText(String relativeRef, String content) {
        return saveBytes(relativeRef, content.getBytes(StandardCharsets.UTF_8));
    }

    public String saveBytes(String relativeRef, byte[] content) {
        try {
            Path target = safeResolve(relativeRef);
            Files.createDirectories(target.getParent());
            Files.write(target, content);
            return relativeRef;
        } catch (IOException e) {
            log.warn("Failed to persist storage ref {}: {}", relativeRef, e.getMessage());
            return relativeRef;
        }
    }

    /** Resolve a storage ref to a readable Resource: filesystem first, then classpath fallback. */
    public Resource resolve(String ref) {
        if (ref == null || ref.isBlank()) {
            return null;
        }
        try {
            Path p = safeResolve(ref);
            if (Files.exists(p) && Files.isReadable(p)) {
                return new FileSystemResource(p);
            }
        } catch (RuntimeException ignored) {
            // fall through to classpath
        }
        Resource cp = new ClassPathResource(ref);
        if (cp.exists()) {
            return cp;
        }
        return null;
    }

    public boolean exists(String ref) {
        Resource r = resolve(ref);
        return r != null && r.exists();
    }

    public static String sha256(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            return Integer.toHexString(content.hashCode());
        }
    }

    private Path safeResolve(String relativeRef) {
        Path resolved = root.resolve(relativeRef).normalize();
        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException("Path traversal rejected: " + relativeRef);
        }
        return resolved;
    }
}
