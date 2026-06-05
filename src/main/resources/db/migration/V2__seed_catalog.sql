-- =====================================================================
-- Seed: catalog domain (manufacturers, models, aliases, specs, variants,
-- images, a catalog source + page + snapshot + evidence, market sources).
-- Versioned (runs once); ON CONFLICT guards keep it safe if partially present.
-- =====================================================================

-- Manufacturers (>= 6)
INSERT INTO manufacturers (name, normalized_name, created_at) VALUES
  ('Honda', 'honda', now()),
  ('Yamaha', 'yamaha', now()),
  ('Suzuki', 'suzuki', now()),
  ('Kawasaki', 'kawasaki', now()),
  ('BMW', 'bmw', now()),
  ('KTM', 'ktm', now())
ON CONFLICT (normalized_name) DO NOTHING;

-- Models (>= 15). normalized_name = full alphanumeric model code (drives matching).
INSERT INTO motorcycle_models (manufacturer_id, name, normalized_name, production_start_year, production_end_year, created_at, updated_at)
SELECT m.id, v.name, v.code, v.y1, v.y2, now(), now()
FROM (VALUES
  ('honda',    'Honda CBR600RR',          'cbr600rr',   2003, 2022),
  ('honda',    'Honda Transalp',          'transalp',   2023, 2024),
  ('honda',    'Honda CB500F',            'cb500f',     2013, 2024),
  ('honda',    'Honda Africa Twin',       'africatwin', 2020, 2024),
  ('honda',    'Honda CBR1000RR',         'cbr1000rr',  2004, 2024),
  ('yamaha',   'Yamaha YZF-R6',           'yzfr6',      1999, 2020),
  ('yamaha',   'Yamaha MT-07',            'mt07',       2014, 2024),
  ('yamaha',   'Yamaha MT-09',            'mt09',       2013, 2024),
  ('yamaha',   'Yamaha YZF-R1',           'yzfr1',      1998, 2024),
  ('suzuki',   'Suzuki GSX-R600',         'gsxr600',    2001, 2024),
  ('suzuki',   'Suzuki GSX-R750',         'gsxr750',    2000, 2024),
  ('suzuki',   'Suzuki V-Strom 650',      'vstrom650',  2004, 2024),
  ('kawasaki', 'Kawasaki Ninja ZX-6R',    'ninjazx6r',  2009, 2024),
  ('kawasaki', 'Kawasaki Z900',           'z900',       2017, 2024),
  ('kawasaki', 'Kawasaki Versys 650',     'versys650',  2007, 2024),
  ('bmw',      'BMW R 1250 GS',           'r1250gs',    2019, 2024),
  ('bmw',      'BMW S 1000 RR',           's1000rr',    2009, 2024),
  ('ktm',      'KTM 390 Duke',            'duke390',    2013, 2024),
  ('ktm',      'KTM 1290 Super Duke R',   'superduke1290', 2014, 2024)
) AS v(manuf, name, code, y1, y2)
JOIN manufacturers m ON m.normalized_name = v.manuf
ON CONFLICT (manufacturer_id, normalized_name) DO NOTHING;

-- Aliases (incl. the §7 matching-test aliases). normalized_alias = compact form.
INSERT INTO model_aliases (model_id, alias, normalized_alias)
SELECT mo.id, a.alias, a.norm
FROM (VALUES
  ('cbr600rr',  'CBR 600 RR',  'cbr600rr'),
  ('cbr600rr',  'CBR600RR',    'cbr600rr'),
  ('cbr1000rr', 'CBR 1000 RR', 'cbr1000rr'),
  ('transalp',  'Trans Alp',   'transalp'),
  ('transalp',  'XL750 Transalp', 'xl750transalp'),
  ('cb500f',    'CB 500 F',    'cb500f'),
  ('africatwin','Africa Twin', 'africatwin'),
  ('africatwin','CRF1100L',    'crf1100l'),
  ('yzfr6',     'R6',          'r6'),
  ('yzfr6',     'YZF R6',      'yzfr6'),
  ('mt07',      'MT 07',       'mt07'),
  ('mt07',      'MT07',        'mt07'),
  ('mt09',      'MT 09',       'mt09'),
  ('yzfr1',     'R1',          'r1'),
  ('yzfr1',     'YZF R1',      'yzfr1'),
  ('gsxr600',   'GSXR 600',    'gsxr600'),
  ('gsxr600',   'GSX R 600',   'gsxr600'),
  ('gsxr750',   'GSXR 750',    'gsxr750'),
  ('vstrom650', 'V Strom 650', 'vstrom650'),
  ('vstrom650', 'DL650',       'dl650'),
  ('ninjazx6r', 'ZX6R',        'zx6r'),
  ('ninjazx6r', 'ZX 6R',       'zx6r'),
  ('z900',      'Z 900',       'z900'),
  ('versys650', 'Versys 650',  'versys650'),
  ('r1250gs',   'R1250 GS',    'r1250gs'),
  ('r1250gs',   'GS 1250',     'gs1250'),
  ('s1000rr',   'S1000 RR',    's1000rr'),
  ('duke390',   '390 Duke',    '390duke'),
  ('superduke1290', '1290 Super Duke R', '1290superduke')
) AS a(code, alias, norm)
JOIN motorcycle_models mo ON mo.normalized_name = a.code;

-- Specs (one per model; capacity drives the matching capacity boost).
INSERT INTO motorcycle_specs (model_id, engine_capacity_cc, power_kw, torque_nm, dry_weight_kg, wet_weight_kg,
                              seat_height_mm, fuel_capacity_l, top_speed_kmh, cooling, transmission, final_drive, abs, is_electric)
SELECT mo.id, s.cc, s.kw, s.nm, s.dry, s.wet, s.seat, s.fuel, s.tops, s.cool, s.trans, s.drive, s.abs, false
FROM (VALUES
  ('cbr600rr',  599, 88.0, 66.0, 186, 194, 820, 18.1, 260, 'Liquid', '6-speed', 'Chain', true),
  ('transalp',  755, 67.0, 75.0, 180, 208, 850, 16.9, 200, 'Liquid', '6-speed', 'Chain', true),
  ('cb500f',    471, 35.0, 43.0, 175, 189, 785, 17.1, 180, 'Liquid', '6-speed', 'Chain', true),
  ('africatwin',1084, 75.0, 105.0, 210, 226, 850, 24.8, 215, 'Liquid', '6-speed', 'Chain', true),
  ('cbr1000rr', 999, 160.0, 113.0, 195, 201, 830, 16.1, 299, 'Liquid', '6-speed', 'Chain', true),
  ('yzfr6',     599, 87.0, 61.7, 169, 190, 850, 17.0, 262, 'Liquid', '6-speed', 'Chain', true),
  ('mt07',      689, 54.0, 67.0, 164, 184, 805, 14.0, 206, 'Liquid', '6-speed', 'Chain', true),
  ('mt09',      889, 87.5, 93.0, 169, 189, 825, 14.0, 230, 'Liquid', '6-speed', 'Chain', true),
  ('yzfr1',     998, 147.1, 112.4, 199, 201, 855, 17.0, 299, 'Liquid', '6-speed', 'Chain', true),
  ('gsxr600',   599, 92.0, 70.0, 187, 187, 810, 16.5, 260, 'Liquid', '6-speed', 'Chain', true),
  ('gsxr750',   750, 110.0, 86.0, 190, 190, 810, 17.0, 280, 'Liquid', '6-speed', 'Chain', true),
  ('vstrom650', 645, 52.0, 62.0, 211, 216, 835, 20.0, 190, 'Liquid', '6-speed', 'Chain', true),
  ('ninjazx6r', 636, 95.0, 70.8, 196, 196, 830, 17.0, 260, 'Liquid', '6-speed', 'Chain', true),
  ('z900',      948, 92.2, 98.6, 210, 212, 820, 17.0, 240, 'Liquid', '6-speed', 'Chain', true),
  ('versys650', 649, 50.2, 64.0, 209, 216, 845, 21.0, 195, 'Liquid', '6-speed', 'Chain', true),
  ('r1250gs',   1254, 100.0, 143.0, 239, 249, 850, 20.0, 220, 'Liquid', '6-speed', 'Shaft', true),
  ('s1000rr',   999, 152.2, 113.0, 193, 197, 824, 16.5, 299, 'Liquid', '6-speed', 'Chain', true),
  ('duke390',   373, 32.0, 37.0, 149, 167, 830, 13.4, 167, 'Liquid', '6-speed', 'Chain', true),
  ('superduke1290', 1301, 132.0, 140.0, 189, 198, 835, 16.0, 290, 'Liquid', '6-speed', 'Chain', true)
) AS s(code, cc, kw, nm, dry, wet, seat, fuel, tops, cool, trans, drive, abs)
JOIN motorcycle_models mo ON mo.normalized_name = s.code;

-- A few variants for richer model detail.
INSERT INTO motorcycle_variants (model_id, name, year_from, year_to)
SELECT mo.id, v.name, v.y1, v.y2
FROM (VALUES
  ('cbr600rr', 'CBR600RR ABS', 2009, 2022),
  ('mt07',     'MT-07 ABS',    2014, 2024),
  ('r1250gs',  'R 1250 GS Adventure', 2019, 2024),
  ('z900',     'Z900 SE',      2022, 2024)
) AS v(code, name, y1, y2)
JOIN motorcycle_models mo ON mo.normalized_name = v.code;

-- Images for a few models (classpath-resolved seed media so they render on first boot).
INSERT INTO motorcycle_images (model_id, storage_ref, source_url, width, height)
SELECT mo.id, i.ref, NULL, 400, 250
FROM (VALUES
  ('cbr600rr', 'seed-media/honda-cbr600rr.svg'),
  ('mt07',     'seed-media/yamaha-mt07.svg'),
  ('gsxr600',  'seed-media/suzuki-gsxr600.svg'),
  ('ninjazx6r','seed-media/kawasaki-zx6r.svg'),
  ('r1250gs',  'seed-media/bmw-r1250gs.svg')
) AS i(code, ref)
JOIN motorcycle_models mo ON mo.normalized_name = i.code;

-- Catalog source + one page + one snapshot, then wire evidence for two models so
-- their detail page shows a source reference.
INSERT INTO catalog_sources (name, source_type, access_method, base_url, enabled, compliance_status, schedule_cron)
VALUES ('motorcyclespecs.co.za', 'catalog', 'scrape', 'https://www.motorcyclespecs.co.za/', false, 'allowed', NULL);

INSERT INTO catalog_source_pages (source_id, url, page_type, discovered_at)
SELECT s.id, 'https://www.motorcyclespecs.co.za/model/honda/honda_cbr600rr.htm', 'model', now()
FROM catalog_sources s WHERE s.name = 'motorcyclespecs.co.za';

INSERT INTO catalog_source_snapshots (page_id, storage_ref, fetched_at, http_status, content_hash)
SELECT p.id, 'catalog/motorcyclespecs/seed-cbr600rr.html', now(), 200, 'seedhash-cbr600rr'
FROM catalog_source_pages p
WHERE p.url = 'https://www.motorcyclespecs.co.za/model/honda/honda_cbr600rr.htm';

INSERT INTO spec_evidence (spec_id, snapshot_id, field_name, raw_value)
SELECT sp.id, sn.id, 'Capacity', '599 cc'
FROM motorcycle_specs sp
JOIN motorcycle_models mo ON mo.id = sp.model_id AND mo.normalized_name = 'cbr600rr'
CROSS JOIN catalog_source_snapshots sn
WHERE sn.storage_ref = 'catalog/motorcyclespecs/seed-cbr600rr.html';

-- Market sources: OLX (CSV import path) + Facebook (blocked, future). Both disabled.
INSERT INTO market_sources (name, source_type, access_method, base_url, enabled, compliance_status, schedule_cron)
VALUES
  ('OLX', 'marketplace', 'csv_import', 'https://www.olx.ro', false, 'needs_review', NULL),
  ('Autovit', 'marketplace', 'csv_import', 'https://www.autovit.ro', false, 'needs_review', NULL),
  ('Facebook Marketplace', 'marketplace', 'browser_assisted', 'https://www.facebook.com/marketplace', false, 'blocked', NULL);

INSERT INTO market_search_queries (market_source_id, query, enabled)
SELECT ms.id, q.query, false
FROM (VALUES ('OLX', 'motocicleta'), ('OLX', 'honda cbr'), ('Autovit', 'yamaha mt')) AS q(src, query)
JOIN market_sources ms ON ms.name = q.src;
