# Master script to start the whole stack locally (Postgres/Keycloak optional) and the three microservices as jars
param(
    [switch]$StartDocker = $true,
    [int]$DelayBetweenStarts = 4
)

$scriptFolder = Split-Path -Parent $MyInvocation.MyCommand.Definition
# repo root is parent of the scripts folder
$root = Resolve-Path (Join-Path $scriptFolder "..")
Write-Host "Running from repo root: $root"

# Clean target directories BEFORE starting (from repo root)
Write-Host "Cleaning target directories..."
$targetDirs = @(
    (Join-Path $root "ms-solicitudes" "target"),
    (Join-Path $root "ms-logistica" "target"),
    (Join-Path $root "ms-gateway" "target")
)

foreach ($dir in $targetDirs) {
    if (Test-Path $dir) {
        Remove-Item -Recurse -Force $dir | Out-Null
        Write-Host "   Removed $dir"
    }
}

if ($StartDocker) {
    if (Test-Path -Path (Join-Path $root "docker-compose.yml")) {
        Write-Host "Starting docker-compose services (Postgres, Keycloak if configured)..."
        cd $root
        docker compose up -d
        Write-Host "docker-compose up -d issued. Waiting 10s for services to settle..."
        Start-Sleep -Seconds 10
    } else {
        Write-Warning "No docker-compose.yml found at repo root. Skipping Docker startup."
    }
}

# Start ms-logistica
Write-Host "Starting ms-logistica (jar)"
cd (Join-Path $root "ms-logistica")
Start-Process -FilePath powershell -ArgumentList "-ExecutionPolicy Bypass -File .\scripts\run-jar-ms-logistica.ps1" -WindowStyle Hidden
Start-Sleep -Seconds $DelayBetweenStarts

# Start ms-solicitudes
Write-Host "Starting ms-solicitudes (jar)"
cd (Join-Path $root "ms-solicitudes")
Start-Process -FilePath powershell -ArgumentList "-ExecutionPolicy Bypass -File .\scripts\run-jar-ms-solicitudes.ps1" -WindowStyle Hidden
Start-Sleep -Seconds $DelayBetweenStarts

# Start ms-gateway
Write-Host "Starting ms-gateway (jar)"
cd (Join-Path $root "ms-gateway")
Start-Process -FilePath powershell -ArgumentList "-ExecutionPolicy Bypass -File .\scripts\run-jar-ms-gateway.ps1" -WindowStyle Hidden

Write-Host "All start requests issued. Use the logs in each module's ./logs folder to monitor."
