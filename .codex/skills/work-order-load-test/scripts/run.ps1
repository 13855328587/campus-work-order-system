param(
    [ValidateSet("admin", "student", "worker")]
    [string]$Role = "admin",
    [string]$AuthToken
)

$ErrorActionPreference = "Stop"

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..\..\..")).Path
$runner = Join-Path $projectRoot "performance\run-test.ps1"

if (-not (Test-Path $runner)) {
    throw "Performance runner not found: $runner"
}

$paths = @{
    admin = "/api/work-orders/page?pageNum=1&pageSize=5&status=PENDING_REVIEW"
    student = "/api/work-orders/my?pageNum=1&pageSize=5"
    worker = "/api/work-orders/worker/tasks?pageNum=1&pageSize=5"
}

if ([string]::IsNullOrWhiteSpace($AuthToken)) {
    $AuthToken = Read-Host "Paste login token (without Bearer)"
}

if ([string]::IsNullOrWhiteSpace($AuthToken)) {
    throw "Token cannot be empty"
}

& $runner -AuthToken $AuthToken -TestPath $paths[$Role]

if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

& (Join-Path $PSScriptRoot "analyze-result.ps1")
