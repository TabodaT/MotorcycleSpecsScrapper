package com.motointel.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Serves the built React SPA from classpath:/static and forwards SPA deep links
 * (any non-{@code /api}, non-asset path) to index.html so reloads on routes like
 * /analytics or /match-review resolve instead of 404ing.
 *
 * Controller mappings (incl. /api/**) are matched with higher priority than this
 * resource handler, so API routes are never shadowed; unknown /api paths return 404.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new SpaResourceResolver());
    }

    private static final class SpaResourceResolver extends PathResourceResolver {
        private final Resource index = new ClassPathResource("/static/index.html");

        @Override
        protected Resource getResource(String resourcePath, Resource location) throws IOException {
            Resource requested = location.createRelative(resourcePath);
            if (requested.exists() && requested.isReadable()) {
                return requested;
            }
            // Never serve the SPA shell for API calls — let the dispatcher 404 them.
            if (resourcePath.startsWith("api/") || resourcePath.startsWith("actuator/")) {
                return null;
            }
            // SPA fallback for client-side routes (deep links / reloads).
            return index.exists() ? index : null;
        }
    }
}
