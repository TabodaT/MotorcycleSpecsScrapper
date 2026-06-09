package com.motointel.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Enables {@code @Async} and provides the dedicated executor for catalog crawls.
 * Catalog ingestion is a long-running, polite (rate-limited) crawl of the whole source, so it must
 * not block the HTTP request thread. A single worker thread keeps concurrent ingest requests
 * serialized (we never want two crawls hammering the source at once); extra requests queue.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    public static final String CATALOG_EXECUTOR = "catalogExecutor";

    @Bean(name = CATALOG_EXECUTOR)
    public Executor catalogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(8);
        executor.setThreadNamePrefix("catalog-crawl-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
