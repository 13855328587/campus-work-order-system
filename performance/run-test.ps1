param(
    [string]$AuthToken,
    [string]$TestPath = "/api/work-orders/page?pageNum=1&pageSize=5&status=PENDING_REVIEW"
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($AuthToken)) {
    $AuthToken = Read-Host "Paste login token (without Bearer)"
}

if ([string]::IsNullOrWhiteSpace($AuthToken)) {
    throw "Token cannot be empty"
}

$performanceDir = $PSScriptRoot.Replace('\', '/')

Write-Host "Starting load test: 17 requests/second for 1 minute..." -ForegroundColor Cyan

docker run --rm `
    -e BASE_URL=http://host.docker.internal:8080 `
    -e AUTH_TOKEN=$AuthToken `
    -e TEST_PATH=$TestPath `
    -v "${performanceDir}:/scripts" `
    grafana/k6:0.54.0 run `
    --summary-export=/scripts/results/summary.json `
    /scripts/work-order-read.js

if ($LASTEXITCODE -ne 0) {
    throw "Load test failed. Exit code: $LASTEXITCODE"
}

Write-Host "Load test passed. Result: performance/results/summary.json" -ForegroundColor Green
