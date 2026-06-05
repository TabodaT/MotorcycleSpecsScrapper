# Smoke test for the running stack. Usage: ./smoke.ps1 [base-url]
$ErrorActionPreference = "Stop"
$base = if ($args.Count -ge 1) { $args[0] } else { "http://localhost:18080" }
$csv = Join-Path $PSScriptRoot "demo_listings.csv"

Write-Host "== 1. health =="
$health = curl.exe -s "$base/api/health" | ConvertFrom-Json
Write-Host "status=$($health.status) db=$($health.db) version=$($health.version)"
if ($health.status -ne "UP") { throw "FAIL: health not UP" }

Write-Host "== 2. import template =="
$tpl = curl.exe -s "$base/api/market/import/template"
Write-Host $tpl

Write-Host "== 3. import demo CSV =="
$import = curl.exe -s -X POST "$base/api/market/import/csv" -F "file=@$csv" | ConvertFrom-Json
Write-Host "success=$($import.success) inserted=$($import.data.inserted) updated=$($import.data.updated) failed=$($import.data.failed)"
if ([int]$import.data.inserted -lt 1) { throw "FAIL: expected inserted >= 1" }

Write-Host "== 4. match review (expect >= 1 needs_review) =="
$review = curl.exe -s "$base/api/matching/review?page=0&size=50" | ConvertFrom-Json
Write-Host "needs_review total = $($review.meta.total)"
if ([int]$review.meta.total -lt 1) { throw "FAIL: expected a needs_review listing" }

Write-Host "== 5. analytics summary (expect non-zero counts) =="
$an = curl.exe -s "$base/api/analytics/summary" | ConvertFrom-Json
Write-Host "listingCount=$($an.data.listingCount) activeCount=$($an.data.activeCount) medianPrice=$($an.data.medianPrice)"
if ([int]$an.data.listingCount -lt 1) { throw "FAIL: expected non-zero listingCount" }

Write-Host ""
Write-Host "SMOKE PASSED"
