# /goal (HARDENED) — Refactor MotorcycleSpecsScrapper into a Ready-to-Use Local Motorcycle Market Intelligence App

> This is a **hardened, drop-in replacement** for the original `/goal` spec. It keeps the original product
> intent but **resolves every either/or fork and pins every loose contract** that would otherwise force the
> implementer to guess. Sections marked **[HARD]** are non-negotiable and override anything vaguer elsewhere.
> The original Definition of Done, anti-loophole rules, and 7-page UI requirements still apply in full.

---

## 0. HARD DECISIONS — resolve all forks before writing any code  [HARD]

The implementer MUST make exactly these choices. No alternatives, no "if feasible" hedging.

| Decision | Pinned value | Rationale |
|---|---|---|
| Database | **PostgreSQL 16 ONLY** | Removes MySQL/Postgres half-migration. The legacy MySQL DDL and `.txt` SQL are **deleted, not ported.** |
| Data access | **Spring Data JPA ONLY** (Hibernate) | jOOQ is **forbidden** (codegen-vs-Flyway build-ordering hazard). |
| Migrations | **Flyway** (`db/migration/V1__*.sql` …) | Liquibase forbidden, to keep one convention. |
| Java | **Java 21** | Drop "if feasible". |
| Spring Boot | **3.3.x** (pin a concrete patch in `pom.xml`, e.g. the latest `3.3.x`) | |
| Build | **Maven** (keep the wrapper) | |
| Frontend | **React 18 + TypeScript + Vite + TanStack Query v5 + Recharts** | |
| Frontend serving | **Built SPA is served as static content BY the Spring app at the same origin** from `src/main/resources/static/` (Vite `base: '/'`). No separate frontend container, no Vite proxy in prod, **no CORS** needed. A catch-all `@Controller` forwards any non-`/api/**`, non-static-asset GET to `/index.html` so **SPA deep links and page reloads** (e.g. `/analytics`, `/match-review`) resolve instead of 404ing. | Kills the #1 "loads but API calls fail" failure class. |
| HTML parsing | **jsoup** (replace HtmlUnit) | Simpler, snapshot-friendly, testable on fixture HTML. |
| Containers | **Two services only:** `app` (multi-stage build) + `db` (postgres:16) | |
| Base images | build: `eclipse-temurin:21-jdk`; frontend build: `node:20`; runtime: `eclipse-temurin:21-jre`; db: `postgres:16` | Pin tags so host vs container can't diverge. |

If any instruction elsewhere conflicts with this table, **this table wins.**

---

## 1. PORTS & ENV — must bind cleanly on the FIRST `docker compose up`  [HARD]

The target machine already has **5432, 3000, and 8000 occupied.** Therefore use these **non-default** host ports,
all overridable via `.env`:

```
APP_HOST_PORT=18080        # Spring app (serves /api + the SPA)
DB_HOST_PORT=55432         # Postgres published to host (container stays 5432)
```

- The user opens exactly: **http://localhost:18080**
- Inside the compose network the app reaches the DB at `db:5432` (NOT the host port).
- Provide `.env.example` with every variable below and sane, collision-free defaults:

```
# --- Database ---
DB_NAME=motointel
DB_USER=motointel
DB_PASSWORD=motointel_local_dev      # local-only dev default; NOT a real secret
DB_HOST_PORT=55432

# --- App ---
APP_HOST_PORT=18080
SPRING_PROFILES_ACTIVE=docker

# --- Storage ---
STORAGE_ROOT=/data/storage           # inside container; backed by a named volume

# --- Scraper / sources ---
SCRAPER_USER_AGENT=MotoIntelBot/1.0 (+local)
HTTP_TIMEOUT_MS=15000
CATALOG_RATE_LIMIT_RPS=1

# --- Listing lifecycle ---
LISTING_REMOVED_THRESHOLD_N=2        # missing in N consecutive scans -> removed
LISTING_LIKELY_SOLD_DAYS_X=7         # active >= X days then vanished -> likely_sold

# --- Scheduler ---
SCHEDULER_DEFAULT_INTERVAL_CRON=0 0 3 * * *   # 03:00 daily; disabled by default
```

- **No real secrets in git.** The dev DB password above is a local-only default and must be documented as such.
- **No machine-specific absolute paths anywhere.** All storage goes through `STORAGE_ROOT`.

---

## 2. DOCKER COMPOSE — must succeed first-try, persist data, no race  [HARD]

`docker-compose.yml` MUST:

1. Define a `db` service (`postgres:16`) that sets `environment: { POSTGRES_USER: ${DB_USER}, POSTGRES_PASSWORD: ${DB_PASSWORD}, POSTGRES_DB: ${DB_NAME} }`, a **named volume** `pgdata:/var/lib/postgresql/data`, and a **healthcheck that references the in-container `POSTGRES_*` names** (not the host `DB_*` names):
   ```yaml
   healthcheck:
     test: ["CMD-SHELL", "pg_isready -U $${POSTGRES_USER} -d $${POSTGRES_DB}"]
     interval: 5s
     timeout: 5s
     retries: 20
   ```
2. Define an `app` service that:
   - builds from a **multi-stage Dockerfile**: stage 1 `node:20` runs `npm ci && npm run build` for the SPA;
     stage 2 `eclipse-temurin:21-jdk` runs `mvn -B package` (the SPA `dist/` is copied into
     **`src/main/resources/static/`** before packaging so the jar serves it at root); final stage
     `eclipse-temurin:21-jre` runs the jar.
   - declares `depends_on: { db: { condition: service_healthy } }`.
   - mounts a **named volume** for storage: `storage:/data/storage`.
   - publishes `${APP_HOST_PORT}:8080`.
3. Spring datasource config MUST include connection **retry** (so a transient DB hiccup doesn't kill boot) and
   Flyway runs on startup.
4. **Persistence proof is mandatory:** the implementer must run `docker compose up -d`, seed/observe data,
   then `docker compose down` (WITHOUT `-v`), then `docker compose up -d` again, and confirm data survived.
   Both row counts (before/after) must be pasted into the final response. `down -v` is the documented RESET.

---

## 3. CANONICAL CSV CONTRACT — one source of truth  [HARD]

There is **exactly one** CSV column contract. `GET /api/market/import/template`, the import validator, the parser,
and the demo seed CSV **all derive from this list. They must be byte-identical on the header row.**

**Header (in this order):**
```
source,url,title,price,currency,observed_at,description,location,posted_date,seller_type,external_id,mileage_km,image_url
```

- **Required (validation error if missing/empty):** `source`, `url`, `title`, `price`, `currency`, `observed_at`
- **Optional:** the rest.
- **Types/validation:** `price` = non-negative decimal (reject otherwise with a row-level error); `currency` =
  3-letter ISO (e.g. `RON`, `EUR`); `observed_at`/`posted_date` = ISO-8601 date or datetime; `mileage_km` = integer ≥ 0 if present.
- **Dedupe key:** `(source, external_id)` when `external_id` is present; otherwise `(source, normalized_url)` where
  `normalized_url` = trim + lowercase scheme/host + strip trailing slash + drop tracking query params.
- **Update semantics:** existing listing → update `last_observed_at`, status back to `active`; if `price` differs from
  the latest `listing_price_history` row → **insert a new price-history row**; always insert a `market_listing_snapshot`.
- After import, **matching runs automatically** for new/changed listings, and the import job result (counts +
  per-row errors) is returned and shown in the UI.

Provide a ready-to-upload **`demo_listings.csv`** with this exact header (see §6).

---

## 4. API CONTRACT — pinned shapes so frontend & backend cannot diverge  [HARD]

- **All JSON is camelCase.** All responses use this envelope:
  ```json
  { "success": true, "data": <payload-or-null>, "error": null,
    "meta": { "total": 0, "page": 0, "size": 0 } }
  ```
  (`meta` present only on paginated lists; otherwise omit or null.)
- Errors: `{ "success": false, "data": null, "error": { "code": "VALIDATION", "message": "...", "details": [...] } }`
  with appropriate HTTP status (400/404/409/422/500).
- **Exception:** `GET /api/health` returns the bare `{ status, db, version }` object (NOT wrapped) so uptime checks and
  the smoke script can parse it directly. Every other endpoint uses the envelope.
- Fields that are joins, not columns (e.g. `match.modelName`, `candidates[].modelName`, `JobDto.source`,
  `ListingDto.source`/`match.modelName`), are **server-side join projections** (`JobDto.source` = the source's
  *name* string, not its id). They have no dedicated column in §5 and the implementer must not look for one.

**Endpoints (functionality must exist; these exact paths + field names are the contract the React app consumes):**

```
GET  /api/health                         -> { status:"UP", db:"UP", version:"..." }

GET  /api/dashboard/summary              -> { manufacturerCount, modelCount, listingCount,
                                              activeCount, removedCount, likelySoldCount,
                                              unmatchedCount, needsReviewCount,
                                              latestJobs:[JobDto], latestErrors:[JobErrorDto] }

GET  /api/catalog/manufacturers          -> [ { id, name, normalizedName, modelCount } ]
GET  /api/catalog/models?manufacturerId&q&page&size -> paginated [ModelDto]
GET  /api/catalog/models/{id}            -> ModelDto{ id, manufacturerName, name, normalizedName,
                                              productionStartYear, productionEndYear,
                                              variants:[VariantDto], specs:[SpecDto], images:[ImageDto],
                                              aliases:[string], sources:[{url, fetchedAt}] }
GET  /api/catalog/search?q
GET  /api/catalog/aliases?modelId

GET  /api/sources                        -> [ { id, name, sourceType, accessMethod, baseUrl,
                                              enabled, complianceStatus, scheduleCron,
                                              lastRunAt, nextRunAt } ]
POST /api/sources/{id}/ingest            -> { jobId }
PATCH /api/sources/{id}                  -> enable/disable + scheduleCron
GET  /api/jobs?type&status&page&size     -> paginated [JobDto]
GET  /api/jobs/{id}                      -> JobDto{ id, type, source, status, startedAt, finishedAt,
                                              counts:{ discovered, fetched, parsed, inserted, updated, failed },
                                              errorSummary, errors:[JobErrorDto] }

GET  /api/market/listings?source&status&manufacturerId&matchStatus&q&page&size -> paginated [ListingDto]
GET  /api/market/listings/{id}           -> ListingDto{ id, source, url, title, description, price, currency,
                                              location, sellerType, postedDate, firstObservedAt, lastObservedAt,
                                              status, extracted:{ year, manufacturer, modelText, capacityCc, mileageKm },
                                              match:{ status, modelId, modelName, variantId, confidence, explanation },
                                              priceHistory:[{ price, currency, observedAt }],
                                              statusHistory:[{ status, at }], images:[ImageDto], snapshotRef }
POST /api/market/import/csv  (multipart)  -> { jobId, inserted, updated, failed, rowErrors:[{ row, field, message }] }
GET  /api/market/import/template          -> text/csv with the §3 header

GET  /api/matching/review?page&size       -> paginated [ListingDto] where match.status='needs_review'
GET  /api/matching/listings/{id}/candidates -> [ { modelId, modelName, variantId, confidence, explanation } ]
POST /api/matching/listings/{id}/accept   { modelId, variantId? }  -> ListingDto
POST /api/matching/listings/{id}/reject   -> ListingDto
POST /api/matching/listings/{id}/ignore   -> ListingDto
POST /api/matching/listings/{id}/manual-match { modelId, variantId?, createAlias?:bool } -> ListingDto

GET  /api/analytics/summary               -> counts + headline numbers
GET  /api/analytics/prices?modelId        -> [ { modelId, modelName, count, minPrice, maxPrice, avgPrice, medianPrice } ]
GET  /api/analytics/models                -> [ { modelId, modelName, activeCount, totalObserved } ]
GET  /api/analytics/timeline?bucket=week  -> [ { bucket, listed, removed, priceDrops } ]
```

- **Median MUST use Postgres `percentile_cont(0.5)`** (not a client-side or MySQL-incompatible approach).
- Manual matches set `match.status='manual_match'` and are **never overwritten by automatic rematching.**

---

## 5. DATABASE SCHEMA APPENDIX — concrete tables  [HARD]

Author these as Flyway migrations. **Do not reuse `create_moto_models_table.sql` (MySQL-only).** Use Postgres types
(`bigserial`/`identity`, `numeric`, `boolean`, `timestamptz`, `text`). Snake_case table/column names.

**Catalog (Domain 1):**
- `manufacturers(id pk, name text not null, normalized_name text not null unique, created_at timestamptz)`
- `motorcycle_models(id pk, manufacturer_id fk, name text, normalized_name text, production_start_year int,
  production_end_year int, created_at, updated_at)` — unique `(manufacturer_id, normalized_name)`
- `motorcycle_variants(id pk, model_id fk, name text, year_from int, year_to int)`
- `motorcycle_specs(id pk, model_id fk, variant_id fk null, engine_capacity_cc numeric, power_kw numeric,
  torque_nm numeric, dry_weight_kg numeric, wet_weight_kg numeric, seat_height_mm numeric, fuel_capacity_l numeric,
  top_speed_kmh numeric, cooling text, transmission text, final_drive text, abs boolean null, is_electric boolean)`
- `motorcycle_images(id pk, model_id fk, storage_ref text, source_url text, width int null, height int null)`
- `model_aliases(id pk, model_id fk, alias text, normalized_alias text)` — index on `normalized_alias`
- `catalog_sources(id pk, name, source_type, access_method, base_url, enabled bool, compliance_status, schedule_cron,
  last_run_at, next_run_at)`
- `catalog_source_pages(id pk, source_id fk, url text unique, page_type, discovered_at)`
- `catalog_source_snapshots(id pk, page_id fk, storage_ref text, fetched_at, http_status int, content_hash text)`
- `spec_evidence(id pk, spec_id fk, snapshot_id fk, field_name text, raw_value text)`

**Sources / jobs:**
- `ingestion_jobs(id pk, type text, source_id fk null, status text, started_at, finished_at,
  discovered int, fetched int, parsed int, inserted int, updated int, failed int, error_summary text)`
  — index on `status`, `type`, `started_at`
- `ingestion_job_errors(id pk, job_id fk, url text null, stage text, message text, detail text, created_at)`

**Market (Domain 2):**
- `market_sources(id pk, name, source_type, access_method, base_url, enabled, compliance_status, schedule_cron,
  last_run_at, next_run_at)`
- `market_search_queries(id pk, market_source_id fk, query text, enabled bool)`
- `market_listings(id pk, market_source_id fk null, source text, external_id text null, url text, normalized_url text,
  title text, description text, price numeric null, currency text, location text, seller_type text, posted_date date null,
  first_observed_at timestamptz, last_observed_at timestamptz, status text,
  extracted_year int null, extracted_manufacturer text null, extracted_model_text text null, extracted_capacity_cc numeric null,
  extracted_mileage_km int null, latest_snapshot_id bigint null)`
  — **unique `(source, external_id)` where external_id not null; unique `(source, normalized_url)` otherwise**; index on `status`, `price`.
  NOTE: `latest_snapshot_id` is a **circular reference** to `market_listing_snapshots`; do NOT declare it as an inline
  FK (Flyway would fail on first-created table). Either leave it a plain nullable `bigint`, or add the FK via a
  separate `ALTER TABLE ... ADD CONSTRAINT` **after both tables exist**.
- `market_listing_snapshots(id pk, listing_id fk, storage_ref text, captured_at, content_hash text)`
- `market_listing_images(id pk, listing_id fk, storage_ref text, source_url text)`
- `listing_price_history(id pk, listing_id fk, price numeric, currency text, observed_at timestamptz)` — index `(listing_id, observed_at)`
- `listing_status_history(id pk, listing_id fk, status text, at timestamptz)`
- `listing_model_matches(id pk, listing_id fk unique, model_id fk null, variant_id fk null, status text,
  confidence numeric, explanation text, is_manual boolean, decided_at timestamptz)` — index on `status`, `model_id`
- `match_candidates(id pk, listing_id fk, model_id fk, variant_id fk null, confidence numeric, explanation text, rank int)`

(Add any helper columns/indexes needed, but the names above are the contract referenced by §4 and §7.)

---

## 6. DEMO / SEED DATA — explicit, cross-consistent composition  [HARD]

**Seed mechanism is pinned (no fork):** the catalog, aliases, and the listing **match-status mix** are loaded by a
**Flyway seed migration** (`R__seed.sql` repeatable or a dedicated `V*__seed.sql`) so every page is non-empty at boot
**with zero network access**. Re-running is idempotent (guard with `ON CONFLICT DO NOTHING` / existence checks). No
seed HTTP endpoint is required (none is added to §4).

The Flyway seed must load a fixture that makes every page non-empty and every analytic meaningful:

- **≥ 6 manufacturers**: Honda, Yamaha, Suzuki, Kawasaki, BMW, KTM.
- **≥ 15 models** with production year ranges and specs, including the matching test targets: **Honda CBR600RR,
  Suzuki GSX-R600, Yamaha YZF-R6, Yamaha MT-07, Honda Transalp.**
- **Aliases** seeded for those (CBR 600 RR / CBR600RR; GSXR 600 / GSX R 600; R6 / YZF R6; MT 07 / MT07; Trans Alp).
- **≥ 1 image + ≥ 1 catalog snapshot** reference per a few models. Small sample media files are **committed in the repo**
  and **copied into `STORAGE_ROOT` by the seed step** (or the storage abstraction resolves `storage_ref` against a
  classpath fallback when absent from the volume) so images render on first boot even though the `storage` volume starts empty.
- **≥ 30 market listings** spread across **≥ 8 weeks** of `observed_at`/price/status history so the timeline,
  price-drop, days-online, and removed-over-time analytics are non-trivial.
- Match-status mix that is **explicitly guaranteed**: **≥ 5 auto-matched, ≥ 3 needs_review, ≥ 2 unmatched, ≥ 1 manual_match, ≥ 1 ignored.**
- **≥ 4 listings with ≥ 2 price-history rows each**, at least 2 showing a price **drop**.
- A separate, ready-to-upload **`demo_listings.csv`** (the §3 header) is provided so the user can demo the import flow.
  **Its listings use DISTINCT `external_id`s from the Flyway-seeded listings** so that uploading it genuinely
  **inserts new rows** (the §11 smoke can assert `inserted > 0`) rather than dedupe-updating into "0 inserted".

The README must say exactly how to load it (e.g. `docker compose exec app <seed command>` or a `Load demo data`
button calling a documented endpoint). The seed must be **idempotent** (re-running doesn't duplicate).

---

## 7. MATCHING ENGINE — pinned scoring so demo + tests are deterministic  [HARD]

Score ∈ `[0,1]`. Pipeline: normalize text → detect manufacturer → alias/model token match → extract year → extract
capacity → score → apply boosts/penalties → classify.

- **Base similarity (the separating signal):** normalized token/edit similarity between listing text (title+model
  tokens) and `model.normalized_name` **including its full alphanumeric model code** (e.g. `cbr600rr`, `gsxr600`,
  `yzfr6`) and its `model_aliases`. Because the model codes are textually distant, base similarity alone separates
  same-capacity rivals (CBR600RR vs GSX-R600 vs YZF-R6), keeping the winner's margin wide.
- **Boosts are GATED:** year/capacity boosts are added **only to candidates whose base similarity ≥ 0.50** (a
  wrong-brand 600 with low base similarity does NOT ride the capacity boost into the margin). Boost values:
  `+0.10` if extracted year ∈ `[production_start_year, production_end_year]`; `+0.10` if extracted capacity within
  `±5%` of a known variant/spec capacity. Clamp final score to `[0,1]`.
- **Ambiguity penalty:** `−0.15` to the top score if ≥ 2 candidate models with base similarity ≥ 0.50 are within
  `0.10` of the top score.
- **Classification (evaluate in THIS ORDER, first match wins):**
  1. `unmatched` if `top < 0.55`
  2. `matched` (auto) if `top ≥ 0.85` **AND** `(top − second) ≥ 0.15`
  3. otherwise `needs_review` (covers `0.55 ≤ top < 0.85`, and any `top ≥ 0.85` with margin `< 0.15`)

  This ordering makes the §6 demo mix reachable: distinct-code strong listings → `matched`; a deliberately
  abbreviated/ambiguous listing (e.g. a bare "600" with two plausible models within 0.10) → `needs_review`;
  a junk listing scoring `< 0.55` → `unmatched`. The seed author tunes listing text to land each bucket.
- Always persist `match_candidates` (top 5 with `explanation`) and a human-readable `explanation` on the chosen match.
- `manual_match` and `accept` outcomes set `is_manual=true` and are **excluded from automatic rematch.**
- `manual-match` with `createAlias=true` inserts a `model_aliases` row from the listing's model text.
- **Required passing tests** (must use the seeded aliases/models): `CBR 600 RR`→Honda CBR600RR, `CBR600RR`→same,
  `GSXR 600`→Suzuki GSX-R600, `GSX R 600`→same, `R6`/`YZF R6`→Yamaha YZF-R6, `MT 07`/`MT07`→Yamaha MT-07,
  `Trans Alp`→Honda Transalp; plus one deliberately ambiguous listing → `needs_review`; one junk listing → `unmatched`;
  one accepted manual match → not overwritten by a later rematch.

---

## 8. LISTING LIFECYCLE  [HARD]

Statuses: `active, missing_once, removed, likely_sold, expired, unknown, ignored`. Driven by `listing_status_history`
and `LISTING_REMOVED_THRESHOLD_N` / `LISTING_LIKELY_SOLD_DAYS_X` (from `.env`):

- Seen this scan/import → `active` (and append history row if changed).
- Not seen once → `missing_once`.
- Not seen in `N` consecutive scans → `removed`.
- Was active ≥ `X` days then disappeared with no repost detected → `likely_sold`.
- `ignored` only via user action. Do **not** mark every removed listing as sold.

The demo data must include examples of each non-`unknown` status so the dashboard/analytics show them.

---

## 9. CATALOG & MARKETPLACE INGESTION — best-effort, never blocks the demo  [HARD]

- Catalog source (`motorcyclespecs.co.za`) is reimplemented with **jsoup** behind the snapshot-first pipeline
  (`discover → fetch → save raw snapshot → parse → normalize → upsert → evidence`). Parser MUST be **defensive**:
  iterate candidate spec tables instead of hardcoding `table24`; wrap every numeric parse in Optional/try-catch;
  on failure, record an `ingestion_job_errors` row and continue (job ends `completed_with_errors`), never crash.
- **Live ingestion is best-effort and OFF the critical path.** Every DoD item must be satisfiable from **demo/seed
  data with no network.** If the live site is unreachable/changed, the job completes with errors visible in the UI.
- **OLX**: seeded as a `market_source` with `access_method=csv_import` (or `browser_assisted`) and
  `compliance_status=needs_review`, **disabled by default**. CSV import is the supported market path. Document why.
- **Facebook Marketplace**: seeded as a disabled future source, `compliance_status=blocked`, no scraping.
- **Port the following legacy heuristics into a fresh, unit-tested `Normalizer` (do NOT reuse the old mapper wholesale):**
  unit conversions kgm→Nm (×9.80665), ft-lb→Nm (×1.3558), in→mm (×25.4), lb→kg (×0.453592), mph→km/h (×1.609),
  mpg→l/100km (235.21/x), and capacity-from-model-name + electric detection + year-range parsing.
  **Fix the legacy hp→kW bug** (must multiply by ~0.7457) before writing its test.

---

## 10. DELETE LIST — legacy artifacts to remove, not refactor  [HARD]

Explicitly delete (do not migrate): `RunnerWithSpring` scrape-on-boot `CommandLineRunner`; hardcoded image path in
`ModelDetailsService` (`D:/Learning/...`); `application.properties` hardcoded `jdbc:mysql` + `root`/`Lenovo123#`;
`create_moto_models_table.sql` and all `.txt` SQL statement files; the flat `moto_models` table; HtmlUnit dependency;
all hardcoded URL ignore/missing-spec lists in `Constants`; `errors_log.txt` / `not_inserted_models_log.json` (replace
with DB-backed job errors). **DoD check:** `grep` for backticks, `ENGINE=InnoDB`, `Lenovo123`, `D:/Learning`,
`CommandLineRunner` across the final tree must return nothing.

---

## 11. VERIFICATION — runs INSIDE Docker (no host JDK), evidence required  [HARD]

The host has **no JDK**, so all build/test runs happen in containers. The final response MUST paste **real transcripts**,
not hand-written tables:

1. `docker compose build` output (clean state) — proves the multi-stage build + dependency resolution.
2. Backend tests run in-container (e.g. a `mvn -B test` build stage or a one-shot `docker compose run --rm app-test`),
   pasting the JUnit summary line **and a JaCoCo coverage number** (target ≥ 80% on normalization/matching/import).
   Test suites required: normalization, parser (jsoup over committed sample HTML), matching, CSV import, analytics.
3. Frontend: `npm run build` success + `tsc --noEmit` (and any unit checks) output.
4. `docker compose up -d` then a **scripted smoke** (committed `smoke.sh`/`smoke.ps1`) that:
   `curl /api/health`, `GET /api/market/import/template`, `POST` the demo CSV to `/api/market/import/csv`,
   `GET /api/matching/review` (asserts ≥1 needs_review), `GET /api/analytics/summary` (asserts non-zero counts).
   Paste the smoke output.
5. **Restart-persistence:** record a row count → `docker compose down` (no `-v`) → `up` → re-query → paste both counts.
6. (Optional but preferred) a headless Playwright pass over the 7 pages, screenshots saved under `verification/`.

If any step cannot run, say so explicitly — do **not** present unexecuted results as passing.

---

## 12. README — must let the user run everything without editing code  [HARD]

Include, in order: what the app does; **prerequisites (Docker Desktop running; no JDK/Node needed on host)**; the
exact run command (`docker compose up --build`) and the exact URL (**http://localhost:18080**); how to load demo data;
how to import the CSV (with the §3 column list and a link to `demo_listings.csv`); where to review matches; where to
see analytics; how scheduling works; how listing statuses are computed (N/X); how analytics are calculated (incl.
median via `percentile_cont`); **how to change ports** (`.env`); how to reset (`docker compose down -v`); and a
**Known Limitations** section (live catalog scraping is best-effort; OLX/Facebook live ingestion disabled → use CSV).

---

## 13. EVERYTHING ELSE FROM THE ORIGINAL SPEC STILL APPLIES

The original product context, two-domain architecture, durable-snapshot pipeline rule, job system, scheduling,
source-compliance fields, the **7 functional UI pages** (Dashboard, Catalog Browser, Sources/Jobs, CSV Import,
Market Listings, Match Review, Analytics), the **28-item Definition of Done**, the **anti-loophole rules**, and the
**13-phase execution order** remain in force. This hardened spec only **removes ambiguity**; it does not reduce scope.

Implement to completion. The goal is done only when `docker compose up --build` on a clean checkout yields a
browser-usable app at http://localhost:18080 that demonstrates the full loop (dashboard → browse catalog → import CSV
→ auto-match → review/correct → analytics update → survives restart) **on demo data, with zero code or config edits.**
