-- =====================================================================
-- Source configuration (NOT demo content).
-- The app ships EMPTY: no manufacturers, models, specs, or market
-- listings are seeded. These rows only register the integrations so the
-- Sources page has something to point at and a catalog ingest has a base
-- URL to crawl. Populate the catalog by running the catalog ingest from
-- the UI (Sources & Jobs -> Run Ingest) or by importing a market CSV.
-- =====================================================================

-- Catalog source: motorcyclespecs.co.za. Disabled by default so the daily
-- scheduler stays idle; manual "Run Ingest" works regardless of this flag.
INSERT INTO catalog_sources (name, source_type, access_method, base_url, enabled, compliance_status, schedule_cron)
VALUES ('motorcyclespecs.co.za', 'catalog', 'scrape', 'https://www.motorcyclespecs.co.za/', false, 'allowed', NULL);

-- Market sources: OLX / Autovit (CSV import path) + Facebook (blocked, future). All disabled.
INSERT INTO market_sources (name, source_type, access_method, base_url, enabled, compliance_status, schedule_cron)
VALUES
  ('OLX', 'marketplace', 'csv_import', 'https://www.olx.ro', false, 'needs_review', NULL),
  ('Autovit', 'marketplace', 'csv_import', 'https://www.autovit.ro', false, 'needs_review', NULL),
  ('Facebook Marketplace', 'marketplace', 'browser_assisted', 'https://www.facebook.com/marketplace', false, 'blocked', NULL);
