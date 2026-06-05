# Verification transcripts

All build/test runs happen **inside Docker** (the host has no JDK). Raw logs are in this folder:
`build_final.log` (full build), `jacoco.csv` (coverage), `smoke.log`, `persistence.log`,
`endpoints.log` / `endpoints2.log`, `final_check.log`.

Environment: Docker 29.2.1 / Compose v5, host Node 22 (Docker uses node:20), **no host JDK**.

---

## 1. `docker compose build` — multi-stage build (clean)

Stages: `node:20` builds the SPA → `eclipse-temurin:21-jdk` runs `./mvnw -B clean package`
(unit tests + JaCoCo) and copies the SPA `dist/` into `src/main/resources/static/` → final
`eclipse-temurin:21-jre` runs the jar.

```
#27 ... [INFO] Tests run: 48, Failures: 0, Errors: 0, Skipped: 0
#27 ... [INFO] BUILD SUCCESS
#30 naming to docker.io/library/motorcyclespecsscrapper-app:latest done
 Image motorcyclespecsscrapper-app Built
DOCKER_BUILD_EXIT=0
```

## 2. Backend tests (in-container) + JaCoCo coverage

JUnit summary: **48 tests, 0 failures, 0 errors**. Suites: normalization, jsoup parser (over
committed `fixtures/sample_model.html`), matching (all §7 required cases), CSV import, analytics
(timeline assembler), plus URL normalizer, text similarity, and the manual-lock test.

JaCoCo (computed from `jacoco.csv`) on **normalize / matching / imports**:

```
Instructions: 3340/3774 = 88.5%
Lines:        658/727 = 90.5%

Normalizer        89%      MatchingEngine    92%      CsvListingParser  82%
UrlNormalizer     79%      MatchService      94%      CsvImportService  84%
TextSimilarity    90%      CatalogParser     90%      TimelineAssembler 100%
```

> hp→kW fix is asserted directly: `parsePowerKw("80 hp") == 59.66 kW` and **!= 80** (the legacy bug),
> and end-to-end through the jsoup parser fixture.

## 3. Frontend build

```
> tsc && vite build
vite v5.4.11 building for production...
dist/index.html   0.47 kB │ gzip: 0.31 kB
✓ built in 2.88s
```
`tsc --noEmit` passes (strict mode); `dist/index.html` produced and bundled into the jar.

## 4. `docker compose up` + Flyway + scripted smoke

Boot log:
```
Successfully validated 3 migrations
Migrating schema "public" to version "1 - schema"
Migrating schema "public" to version "2 - seed catalog"
Migrating schema "public" to version "3 - seed market"
Successfully applied 3 migrations to schema "public", now at version v3
Started MotoIntelApplication in 9.175 seconds
```

`smoke.sh` output (see `smoke.log`):
```
== 1. health ==        {"status":"UP","db":"UP","version":"1.0.0"}
== 2. import template ==
source,url,title,price,currency,observed_at,description,location,posted_date,seller_type,external_id,mileage_km,image_url
== 3. import demo CSV ==  {"success":true,"data":{"jobId":1,"inserted":8,"updated":0,"failed":0,"rowErrors":[]}...}
== 4. match review ==     (>=1 needs_review listing returned)
== 5. analytics summary ==
  listingCount 32->40, matchedCount 21->26, needsReviewCount 4, unmatchedCount 6, medianPrice 6600.00
SMOKE PASSED
```

Demo CSV uses external ids distinct from the seed, so the import **genuinely inserts 8 new rows**
(`inserted: 8`), and matching runs automatically on them.

## 5. Restart-persistence (down WITHOUT -v, then up)

```
listingCount BEFORE = 40
docker compose down            # volumes retained
docker compose up -d
listingCount AFTER  = 40
PERSISTENCE PROOF PASSED: 40 == 40 (data survived restart)
```
Also confirmed across an **image rebuild** (`final_check.log`): listingCount stayed 40.

## 6. Full-loop API checks (`endpoints.log`, `endpoints2.log`)

- SPA: `GET /` serves `index.html` (`<title>MotoMarket Intelligence</title>`); deep links
  `GET /analytics` and `GET /match-review` → **HTTP 200** (SPA fallback); unknown `GET /api/nope`
  → **HTTP 404** with the error envelope.
- Catalog: manufacturers list; model detail with specs/images/aliases/sources.
- Media: `GET /api/media/seed-media/honda-cbr600rr.svg` → **200, image/svg+xml** (classpath fallback).
- Market: listing detail with `match`, `priceHistory`, `statusHistory`.
- Matching: candidates for a needs_review listing (3 ranked, with explanations); **live Accept**
  locks the match as `matched` + manual ("Accepted by reviewer.").
- Analytics: per-model price stats with `percentile_cont` median (CBR600RR median 6600); weekly timeline.
- Sources: unified catalog + market (OLX `csv_import`/`needs_review`, Facebook `blocked`,
  motorcyclespecs `catalog`), all disabled by default; CSV import job recorded under Jobs.

## 7. §10 delete-list grep (must be empty)

```
ENGINE=InnoDB | Lenovo123 | D:/Learning | CommandLineRunner | jdbc:mysql  -> no matches
MySQL backticks in Java/SQL                                                -> no matches
htmlunit | mysql-connector | jooq | liquibase (pom.xml)                    -> no matches
legacy package com.example                                                 -> not present
```

## 8. Optional (not run)

Headless Playwright screenshot pass over the 7 pages was **not run** (optional per §11.6); the SPA is
confirmed served at `/` with working deep-link fallback, and the frontend build + `tsc` are green.
The pages can be viewed live at http://localhost:18080.
