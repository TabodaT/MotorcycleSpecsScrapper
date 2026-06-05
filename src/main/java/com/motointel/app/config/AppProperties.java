package com.motointel.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Strongly-typed binding of the {@code app.*} configuration tree.
 * All values are environment-overridable (see application.yml / .env).
 */
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String version = "1.0.0";
    private Storage storage = new Storage();
    private Scraper scraper = new Scraper();
    private Listing listing = new Listing();
    private Scheduler scheduler = new Scheduler();

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public Storage getStorage() { return storage; }
    public void setStorage(Storage storage) { this.storage = storage; }
    public Scraper getScraper() { return scraper; }
    public void setScraper(Scraper scraper) { this.scraper = scraper; }
    public Listing getListing() { return listing; }
    public void setListing(Listing listing) { this.listing = listing; }
    public Scheduler getScheduler() { return scheduler; }
    public void setScheduler(Scheduler scheduler) { this.scheduler = scheduler; }

    public static class Storage {
        private String root = "./data/storage";
        public String getRoot() { return root; }
        public void setRoot(String root) { this.root = root; }
    }

    public static class Scraper {
        private String userAgent = "MotoIntelBot/1.0 (+local)";
        private int httpTimeoutMs = 15000;
        private int rateLimitRps = 1;
        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
        public int getHttpTimeoutMs() { return httpTimeoutMs; }
        public void setHttpTimeoutMs(int httpTimeoutMs) { this.httpTimeoutMs = httpTimeoutMs; }
        public int getRateLimitRps() { return rateLimitRps; }
        public void setRateLimitRps(int rateLimitRps) { this.rateLimitRps = rateLimitRps; }
    }

    public static class Listing {
        private int removedThresholdN = 2;
        private int likelySoldDaysX = 7;
        public int getRemovedThresholdN() { return removedThresholdN; }
        public void setRemovedThresholdN(int removedThresholdN) { this.removedThresholdN = removedThresholdN; }
        public int getLikelySoldDaysX() { return likelySoldDaysX; }
        public void setLikelySoldDaysX(int likelySoldDaysX) { this.likelySoldDaysX = likelySoldDaysX; }
    }

    public static class Scheduler {
        private String defaultCron = "0 0 3 * * *";
        public String getDefaultCron() { return defaultCron; }
        public void setDefaultCron(String defaultCron) { this.defaultCron = defaultCron; }
    }
}
