-- =====================================================================
-- Seed: market domain. 32 listings spread over ~10 weeks with an explicit
-- match-status mix (matched / needs_review / unmatched / manual_match / ignored),
-- lifecycle examples, price-drop history, and review candidates.
-- Match statuses are set DIRECTLY here (the seed does not run the engine).
-- =====================================================================

INSERT INTO market_listings (market_source_id, source, external_id, url, normalized_url, title, description,
    price, currency, location, seller_type, posted_date, first_observed_at, last_observed_at, status,
    extracted_year, extracted_manufacturer, extracted_model_text, extracted_capacity_cc, extracted_mileage_km,
    consecutive_missing_count, latest_snapshot_id)
SELECT ms.id, d.source, d.ext,
       'https://example.test/' || lower(d.source) || '/' || d.ext,
       'https://example.test/' || lower(d.source) || '/' || d.ext,
       d.title, d.descr, d.price, d.currency, d.loc, d.seller,
       (now() - (d.wfirst * interval '7 days'))::date,
       now() - (d.wfirst * interval '7 days'),
       now() - (d.wlast * interval '7 days'),
       d.status, d.eyear, d.emanuf, d.title, d.ecc, d.emile, d.mmiss, NULL
FROM (VALUES
  ('seed-001','OLX','Honda CBR600RR 2008 low km','Well kept, new tyres',6500,'EUR','Bucuresti','private',6,0,'active',2008,'Honda',599,32000,0),
  ('seed-002','OLX','Honda CBR 600 RR 2012','Second owner',7200,'EUR','Cluj','private',3,0,'active',2012,'Honda',599,28000,0),
  ('seed-003','Autovit','Yamaha MT-07 2019 ABS','Dealer serviced',6900,'EUR','Timisoara','dealer',5,0,'active',2019,'Yamaha',689,18000,0),
  ('seed-004','OLX','Yamaha MT 07 2021','Like new',7500,'EUR','Iasi','private',5,0,'active',2021,'Yamaha',689,9000,0),
  ('seed-005','Autovit','Suzuki GSX-R600 2010','Track ready',5800,'EUR','Brasov','private',4,0,'active',2010,'Suzuki',599,36000,0),
  ('seed-006','OLX','Yamaha YZF-R6 2015','Akrapovic exhaust',8200,'EUR','Bucuresti','private',6,0,'active',2015,'Yamaha',599,21000,0),
  ('seed-007','OLX','Kawasaki Ninja ZX-6R 2018','One owner',8900,'EUR','Constanta','dealer',2,0,'active',2018,'Kawasaki',636,12000,0),
  ('seed-008','Autovit','BMW R 1250 GS 2020','Full luggage',17500,'EUR','Cluj','dealer',7,0,'active',2020,'BMW',1254,25000,0),
  ('seed-009','OLX','Honda Africa Twin 2021','Adventure ready',13500,'EUR','Bucuresti','private',3,0,'active',2021,'Honda',1084,14000,0),
  ('seed-010','OLX','KTM 390 Duke 2020','Great starter bike',4200,'EUR','Iasi','private',8,0,'active',2020,'KTM',373,16000,0),
  ('seed-011','Autovit','Kawasaki Z900 2019','Stunning condition',8700,'EUR','Timisoara','dealer',1,0,'active',2019,'Kawasaki',948,11000,0),
  ('seed-012','OLX','Suzuki V-Strom 650 2017','Touring setup',6300,'EUR','Brasov','private',9,0,'active',2017,'Suzuki',645,41000,0),
  ('seed-013','OLX','Honda CBR 600 - year unclear','Some parts missing',5200,'EUR','Bucuresti','private',4,0,'active',2007,'Honda',NULL,NULL,0),
  ('seed-014','Autovit','600cc supersport project','Non runner project',3500,'EUR','Cluj','private',5,0,'active',NULL,NULL,600,NULL,0),
  ('seed-015','OLX','Yamaha R sportbike','Needs TLC',7000,'EUR','Iasi','private',2,0,'active',NULL,'Yamaha',NULL,NULL,0),
  ('seed-016','OLX','GSX 600 needs work','Selling as is',2800,'EUR','Constanta','private',6,0,'active',NULL,'Suzuki',600,NULL,0),
  ('seed-017','OLX','Quad ATV 250 farm','Utility quad',2200,'EUR','Brasov','private',3,0,'active',2015,NULL,250,5000,0),
  ('seed-018','Autovit','Scooter 50cc city','Cheap commuter',900,'EUR','Bucuresti','private',7,0,'active',2019,NULL,50,8000,0),
  ('seed-019','OLX','Dirt pit bike parts lot','Spares only',600,'EUR','Cluj','private',5,0,'active',NULL,NULL,NULL,NULL,0),
  ('seed-020','OLX','Honda Fireblade track bike','Race prepped',9500,'EUR','Timisoara','private',4,0,'active',2016,'Honda',999,22000,0),
  ('seed-021','Autovit','MT09 streetfighter custom','Custom build',8000,'EUR','Iasi','private',2,0,'active',2018,'Yamaha',889,15000,0),
  ('seed-022','OLX','spam listing please ignore','junk',1,'EUR','Bucuresti','private',6,2,'ignored',NULL,NULL,NULL,NULL,0),
  ('seed-023','OLX','duplicate repost test','duplicate',5000,'EUR','Cluj','private',3,1,'ignored',NULL,NULL,NULL,NULL,0),
  ('seed-024','Autovit','Suzuki GSX-R750 2014','Recently serviced',6800,'EUR','Brasov','private',5,1,'missing_once',2014,'Suzuki',750,30000,1),
  ('seed-025','OLX','Honda CB500F 2018','Commuter bike',4500,'EUR','Bucuresti','private',9,2,'removed',2018,'Honda',471,19000,2),
  ('seed-026','OLX','Yamaha YZF-R1 2016','Big bird',11000,'EUR','Cluj','private',10,2,'likely_sold',2016,'Yamaha',998,17000,2),
  ('seed-027','Autovit','BMW S1000RR 2015','Superbike',12500,'EUR','Timisoara','dealer',8,3,'expired',2015,'BMW',999,23000,0),
  ('seed-028','OLX','KTM 1290 Super Duke 2019','The Beast',11500,'EUR','Iasi','private',7,2,'unknown',2019,'KTM',1301,20000,0),
  ('seed-029','OLX','Honda CBR600RR 2006','High miles but solid',5500,'EUR','Constanta','private',10,0,'active',2006,'Honda',599,44000,0),
  ('seed-030','Autovit','Yamaha MT-07 2018','Dealer trade-in',6200,'EUR','Brasov','dealer',8,0,'active',2018,'Yamaha',689,26000,0),
  ('seed-031','OLX','Kawasaki Versys 650 2016','Great tourer',5200,'EUR','Bucuresti','private',6,0,'active',2016,'Kawasaki',649,38000,0),
  ('seed-032','OLX','Suzuki GSX-R600 2008','Track day bike',4800,'EUR','Cluj','private',9,0,'active',2008,'Suzuki',599,47000,0)
) AS d(ext, source, title, descr, price, currency, loc, seller, wfirst, wlast, status, eyear, emanuf, ecc, emile, mmiss)
LEFT JOIN market_sources ms ON ms.name = d.source;

-- Match rows (statuses preset to guarantee the §6 demo mix).
INSERT INTO listing_model_matches (listing_id, model_id, variant_id, status, confidence, explanation, is_manual, decided_at)
SELECT l.id, mo.id, NULL, mm.status, mm.conf, mm.expl, mm.manual, now() - (mm.wk * interval '7 days')
FROM (VALUES
  ('seed-001','cbr600rr','matched',0.95,'Auto-matched on code cbr600rr + year + capacity boost.',false,1),
  ('seed-002','cbr600rr','matched',0.93,'Auto-matched on code cbr600rr.',false,1),
  ('seed-003','mt07','matched',0.96,'Auto-matched on code mt07 + year + capacity.',false,1),
  ('seed-004','mt07','matched',0.94,'Auto-matched on code mt07.',false,1),
  ('seed-005','gsxr600','matched',0.92,'Auto-matched on code gsxr600.',false,1),
  ('seed-006','yzfr6','matched',0.95,'Auto-matched on code yzfr6.',false,1),
  ('seed-007','ninjazx6r','matched',0.90,'Auto-matched on alias zx6r.',false,1),
  ('seed-008','r1250gs','matched',0.97,'Auto-matched on code r1250gs.',false,1),
  ('seed-009','africatwin','matched',0.93,'Auto-matched on alias africatwin.',false,1),
  ('seed-010','duke390','matched',0.90,'Auto-matched on alias 390 duke.',false,1),
  ('seed-011','z900','matched',0.94,'Auto-matched on code z900.',false,1),
  ('seed-012','vstrom650','matched',0.92,'Auto-matched on alias v strom 650.',false,1),
  ('seed-013','cbr600rr','needs_review',0.72,'Ambiguous 600 — top candidate within review band.',false,1),
  ('seed-014',NULL,'needs_review',0.62,'Generic 600cc supersport — no clear winner.',false,1),
  ('seed-015','yzfr6','needs_review',0.68,'"Yamaha R" is ambiguous between R6 and R1.',false,1),
  ('seed-016','gsxr600','needs_review',0.70,'"GSX 600" near-matches GSX-R600 within review band.',false,1),
  ('seed-017',NULL,'unmatched',0.30,'Quad/ATV — below match threshold.',false,1),
  ('seed-018',NULL,'unmatched',0.25,'Scooter — below match threshold.',false,1),
  ('seed-019',NULL,'unmatched',0.20,'Parts lot — below match threshold.',false,1),
  ('seed-020','cbr1000rr','manual_match',1.00,'Manually matched: Fireblade = CBR1000RR.',true,1),
  ('seed-021','mt09','manual_match',1.00,'Manually matched on code mt09.',true,1),
  ('seed-022',NULL,'ignored',NULL,'Ignored by reviewer (spam).',true,1),
  ('seed-023',NULL,'ignored',NULL,'Ignored by reviewer (duplicate).',true,1),
  ('seed-024','gsxr750','matched',0.93,'Auto-matched on code gsxr750.',false,1),
  ('seed-025','cb500f','matched',0.94,'Auto-matched on code cb500f.',false,1),
  ('seed-026','yzfr1','matched',0.95,'Auto-matched on code yzfr1.',false,1),
  ('seed-027','s1000rr','matched',0.96,'Auto-matched on code s1000rr.',false,1),
  ('seed-028','superduke1290','matched',0.92,'Auto-matched on alias 1290 super duke.',false,1),
  ('seed-029','cbr600rr','matched',0.91,'Auto-matched on code cbr600rr.',false,1),
  ('seed-030','mt07','matched',0.93,'Auto-matched on code mt07.',false,1),
  ('seed-031','versys650','matched',0.90,'Auto-matched on code versys650.',false,1),
  ('seed-032','gsxr600','matched',0.90,'Auto-matched on code gsxr600.',false,1)
) AS mm(ext, code, status, conf, expl, manual, wk)
JOIN market_listings l ON l.external_id = mm.ext
LEFT JOIN motorcycle_models mo ON mo.normalized_name = mm.code;

-- Candidates for the needs_review listings (so the Match Review page shows options).
INSERT INTO match_candidates (listing_id, model_id, variant_id, confidence, explanation, rank)
SELECT l.id, mo.id, NULL, c.conf, c.expl, c.rank
FROM (VALUES
  ('seed-013','cbr600rr',0.72,'base 0.72 -> score 0.72',1),
  ('seed-013','gsxr600',0.66,'base 0.66 -> score 0.66',2),
  ('seed-013','yzfr6',0.60,'base 0.60 -> score 0.60',3),
  ('seed-014','cbr600rr',0.62,'base 0.62 -> score 0.62',1),
  ('seed-014','gsxr600',0.60,'base 0.60 -> score 0.60',2),
  ('seed-014','yzfr6',0.58,'base 0.58 -> score 0.58',3),
  ('seed-015','yzfr6',0.68,'base 0.68 -> score 0.68',1),
  ('seed-015','yzfr1',0.61,'base 0.61 -> score 0.61',2),
  ('seed-016','gsxr600',0.70,'base 0.70 -> score 0.70',1),
  ('seed-016','gsxr750',0.60,'base 0.60 -> score 0.60',2)
) AS c(ext, code, conf, expl, rank)
JOIN market_listings l ON l.external_id = c.ext
JOIN motorcycle_models mo ON mo.normalized_name = c.code;

-- Price history (>= 4 listings with >= 2 rows; several drops).
INSERT INTO listing_price_history (listing_id, price, currency, observed_at)
SELECT l.id, p.price, p.cur, now() - (p.wk * interval '7 days')
FROM (VALUES
  ('seed-001',7000,'EUR',6),('seed-001',6800,'EUR',3),('seed-001',6500,'EUR',1),
  ('seed-003',7200,'EUR',5),('seed-003',6900,'EUR',2),
  ('seed-006',8500,'EUR',6),('seed-006',8200,'EUR',3),
  ('seed-029',6000,'EUR',10),('seed-029',5800,'EUR',6),('seed-029',5500,'EUR',2),
  ('seed-030',6800,'EUR',8),('seed-030',6500,'EUR',5),('seed-030',6200,'EUR',2)
) AS p(ext, price, cur, wk)
JOIN market_listings l ON l.external_id = p.ext;

-- Status history: initial 'active' for every listing, plus lifecycle transitions.
INSERT INTO listing_status_history (listing_id, status, at)
SELECT l.id, 'active', l.first_observed_at
FROM market_listings l
WHERE l.external_id LIKE 'seed-%';

INSERT INTO listing_status_history (listing_id, status, at)
SELECT l.id, t.status, now() - (t.wk * interval '7 days')
FROM (VALUES
  ('seed-024','missing_once',1),
  ('seed-025','removed',2),
  ('seed-026','likely_sold',2),
  ('seed-027','expired',3),
  ('seed-028','unknown',2),
  ('seed-022','ignored',2),
  ('seed-023','ignored',1)
) AS t(ext, status, wk)
JOIN market_listings l ON l.external_id = t.ext;
