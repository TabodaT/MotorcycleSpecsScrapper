# MotoIntel — Local Motorcycle Market Intelligence

A self-contained web app that combines a **motorcycle spec catalog** with **second-hand market
listings**, automatically matches listings to catalog models, and surfaces pricing & lifecycle
analytics. It runs entirely locally with **one command**. It ships **empty** — populate the catalog
by running the catalog ingest (one click), and add market listings via CSV import.

It's two domains in one app:

1. **Catalog** — manufacturers, models, variants, specs, aliases, images (sourced from
   `motorcyclespecs.co.za`, best-effort).
2. **Market** — listings imported from marketplaces (CSV), deduped, price/status-tracked, and matched
   to catalog models with a deterministic scoring engine.

## What you get (7 pages)

- **Dashboard** — headline counts, latest ingestion jobs and errors.
- **Catalog Browser** — manufacturers → models → full spec/variant/alias/image detail.
- **Sources & Jobs** — enable/disable sources, run ingestion, inspect job results & errors.
- **CSV Import** — download the template, upload a CSV, see per-row results.
- **Market Listings** — filter/search listings, view price history, status history, matches.
- **Match Review** — work the `needs_review` queue: accept / reject / ignore / manual-match.
- **Analytics** — price stats (incl. median), per-model activity, and a weekly timeline.

---

## Prerequisites

- **Docker Desktop** installed and **running**.
- That's it — **no JDK and no Node.js are needed on the host**. Everything builds inside containers.

## Run it

```bash
docker compose up --build
```

Then open: **http://localhost:18080**

The first build compiles the React SPA (`node:20`), builds & tests the Spring Boot jar
(`eclipse-temurin:21-jdk`), and runs it on a slim JRE against PostgreSQL 16. First boot runs Flyway
migrations, which **create the schema** and register the configured sources (the catalog source plus
the market sources). No catalog or market content is seeded — the app starts empty.

> Ports `18080` (app) and `55432` (Postgres) are non-default on purpose so they don't collide with
> common local services. See **Changing ports** below.

## Populating the catalog

The catalog is filled by scraping `motorcyclespecs.co.za` on demand:

1. Open **Sources & Jobs** (`/sources`).
2. On the `motorcyclespecs.co.za` row, click **Run Ingest**.
3. A `catalog_ingest` job starts and runs **in the background** — watch its live counts
   (discovered / fetched / parsed / inserted) in the Jobs table; click the job for per-error detail.

The crawl mirrors the original tool: it walks every manufacturer page and every model on it.

- **First run** (empty DB): fetches **all** models the source lists — this is a long crawl
  (thousands of pages at the configured rate limit), so let it run.
- **Later runs**: fetch **only models that are new** since the last run (already-ingested model URLs
  are skipped), so re-running is fast and just tops up the catalog.

Enabling the source (the **Enabled** toggle) additionally lets the daily scheduler run the same
incremental top-up automatically; manual **Run Ingest** works whether or not the source is enabled.

Tuning (env vars, all optional): `CATALOG_RATE_LIMIT_RPS` (default `1`),
`CATALOG_MAX_MODELS_PER_RUN` (default `0` = unlimited), `CATALOG_MAX_PAGES_PER_MANUFACTURER`
(default `50`).

## Importing a CSV

The supported way to add market listings is CSV import (the marketplaces don't allow automated
scraping — see **Known limitations**).

1. Go to **CSV Import**, click **Download template** (or `GET /api/market/import/template`).
2. Fill it in. The columns (header is fixed and case-sensitive) are:

   ```
   source,url,title,price,currency,observed_at,description,location,posted_date,seller_type,external_id,mileage_km,image_url
   ```

   - **Required:** `source`, `url`, `title`, `price`, `currency`, `observed_at`
   - `price` ≥ 0; `currency` = 3-letter ISO (e.g. `RON`, `EUR`); `observed_at`/`posted_date` =
     ISO-8601 date or datetime; `mileage_km` = integer ≥ 0.
3. Upload it. You'll see how many rows were **inserted / updated / failed**, plus per-row errors.
   New/changed listings are **auto-matched** to catalog models immediately.

A ready-to-upload sample is included: **[`demo_listings.csv`](./demo_listings.csv)** — a quick way to
get listings into an otherwise empty app so the Market, Match Review, and Analytics pages light up.

Dedupe key: `(source, external_id)` when `external_id` is present, otherwise `(source,
normalized_url)`. Re-importing the same listing updates it (and appends a price-history row if the
price changed).

## Reviewing matches

Open **Match Review**. Each `needs_review` listing shows ranked candidate models with a confidence
score and a human-readable explanation. You can **Accept**, **Reject**, **Ignore**, or
**Manual-match** (optionally creating a new alias from the listing text). Manual decisions are
**locked** — automatic rematching never overwrites them.

## Analytics

Open **Analytics**. It shows:

- **Headline stats** — counts + average/median/min/max price.
- **Price by model** — count, min/max/avg and **median price per model**.
- **Model activity** — active vs total-observed listings per model.
- **Timeline** — listings *listed*, *removed*, and *price drops* per week.

**Median is computed in PostgreSQL** via `percentile_cont(0.5) WITHIN GROUP (ORDER BY price)` (exact,
not an approximation).

## How listing statuses are computed

Listings move through a lifecycle driven by observations and two thresholds from `.env`:

- Seen this scan/import → `active`.
- Missed once → `missing_once`.
- Missed in **`LISTING_REMOVED_THRESHOLD_N`** consecutive scans (default **2**) → `removed`.
- Was active for at least **`LISTING_LIKELY_SOLD_DAYS_X`** days (default **7**) then vanished →
  `likely_sold`.
- `ignored` is set only by user action. (Not every removed listing is marked sold.)

## How scheduling works

Each source has an optional cron schedule and an `enabled` flag. A scheduler runs on
`SCHEDULER_DEFAULT_INTERVAL_CRON` (default `0 0 3 * * *`, 03:00 daily) but **only acts on enabled
sources** — so scheduling is effectively **off until you enable a source** on the Sources page. You
can trigger an ingest manually anytime with **Run ingest**.

## Changing ports

Defaults live in `docker-compose.yml`; override them by copying `.env.example` to `.env` and editing:

```
APP_HOST_PORT=18080     # the app (http://localhost:<this>)
DB_HOST_PORT=55432      # Postgres published to the host
```

Inside the compose network the app always reaches Postgres at `db:5432` (not the host port).

## Reset

```bash
docker compose down -v      # removes the named volumes (DB + storage) → back to an empty app on next up
docker compose down         # stops containers but KEEPS data (it survives the restart)
```

## Verification (runs in Docker — no host JDK)

See **[`verification/`](./verification/)** for pasted transcripts: container build, in-container unit
tests + JaCoCo coverage, frontend `tsc`/build, the scripted smoke (`smoke.sh` / `smoke.ps1`), and the
restart-persistence proof. Run the smoke yourself with `./smoke.ps1` (Windows) or `./smoke.sh` (bash)
while the stack is up.

## Known limitations

- **Live catalog scraping is best-effort.** The catalog source (`motorcyclespecs.co.za`) is ingested
  with jsoup behind a snapshot-first pipeline; if the site is unreachable or its layout changed, the
  job completes **`completed_with_errors`** (visible in the UI) with per-page errors, and the rest of
  the catalog that did parse is still ingested.
- **OLX / Facebook Marketplace live ingestion is disabled by design.** Their terms don't permit
  automated scraping, so OLX is wired as a **CSV import** source (compliance `needs_review`) and
  Facebook is a **blocked** future source. Use the **CSV Import** flow for market data.
- Analytics aggregate prices numerically regardless of currency (demo simplification).

## Tech stack

PostgreSQL 16 · Spring Boot 3.3 (Java 21) · Spring Data JPA / Hibernate · Flyway · jsoup ·
React 18 + TypeScript + Vite + TanStack Query + Recharts · Docker (two services: `app` + `db`).
