#!/usr/bin/env bash
# Smoke test for the running stack. Usage: ./smoke.sh [base-url]
set -euo pipefail
BASE="${1:-http://localhost:18080}"
CSV="$(dirname "$0")/demo_listings.csv"

echo "== 1. health =="
HEALTH=$(curl -s "$BASE/api/health")
echo "$HEALTH"
echo "$HEALTH" | grep -q '"status":"UP"' || { echo "FAIL: health not UP"; exit 1; }

echo "== 2. import template =="
curl -s "$BASE/api/market/import/template"; echo

echo "== 3. import demo CSV =="
IMPORT=$(curl -s -X POST "$BASE/api/market/import/csv" -F "file=@$CSV")
echo "$IMPORT"
echo "$IMPORT" | grep -Eq '"inserted":[1-9]' || { echo "FAIL: expected inserted >= 1"; exit 1; }

echo "== 4. match review (expect >= 1 needs_review) =="
REVIEW=$(curl -s "$BASE/api/matching/review?page=0&size=50")
echo "$REVIEW" | head -c 400; echo
echo "$REVIEW" | grep -q '"needs_review"' || { echo "FAIL: expected a needs_review listing"; exit 1; }

echo "== 5. analytics summary (expect non-zero counts) =="
ANALYTICS=$(curl -s "$BASE/api/analytics/summary")
echo "$ANALYTICS"
echo "$ANALYTICS" | grep -Eq '"listingCount":[1-9]' || { echo "FAIL: expected non-zero listingCount"; exit 1; }

echo ""
echo "SMOKE PASSED"
