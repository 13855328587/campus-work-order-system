param(
    [string]$ResultPath
)

$ErrorActionPreference = "Stop"

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..\..\..")).Path
if ([string]::IsNullOrWhiteSpace($ResultPath)) {
    $ResultPath = Join-Path $projectRoot "performance\results\summary.json"
}

if (-not (Test-Path $ResultPath)) {
    throw "Result file not found. Run the load test first: $ResultPath"
}

$result = Get-Content -Raw $ResultPath | ConvertFrom-Json
$metrics = $result.metrics

$requestCount = [int]$metrics.http_reqs.count
$requestRate = [double]$metrics.http_reqs.rate
$httpFailureRate = [double]$metrics.http_req_failed.value
$checkRate = [double]$metrics.checks.value
$businessFailureRate = [double]$metrics.business_failures.value
$averageMs = [double]$metrics.http_req_duration.avg
$p95Ms = [double]$metrics.http_req_duration.'p(95)'

$passed = $requestCount -ge 1000 `
    -and $httpFailureRate -lt 0.01 `
    -and $businessFailureRate -lt 0.01 `
    -and $checkRate -gt 0.99 `
    -and $p95Ms -lt 1000

[PSCustomObject]@{
    RequestCount = $requestCount
    RequestsPerSecond = [Math]::Round($requestRate, 2)
    HttpFailurePercent = [Math]::Round($httpFailureRate * 100, 2)
    CheckSuccessPercent = [Math]::Round($checkRate * 100, 2)
    BusinessFailurePercent = [Math]::Round($businessFailureRate * 100, 2)
    AverageLatencyMs = [Math]::Round($averageMs, 2)
    P95LatencyMs = [Math]::Round($p95Ms, 2)
    Passed = $passed
} | Format-List

if (-not $passed) {
    exit 1
}
