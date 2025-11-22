# Run script for ms-gateway: kills any process listening on the configured port and starts the gateway
$port = 8080
Write-Host "Checking port $port..."
try {
    $conn = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    if ($null -ne $conn) {
        $pids = $conn | Select-Object -ExpandProperty OwningProcess -Unique
        foreach ($pid in $pids) {
            Write-Host "Port $port is in use by PID $pid. Stopping process..."
            try {
                Stop-Process -Id $pid -Force -ErrorAction Stop
                Write-Host "Process $pid stopped."
            } catch {
                Write-Warning ("Could not stop process {0}: {1}" -f $pid, $_)
            }
        }
        Start-Sleep -Seconds 1
    } else {
        Write-Host "Port $port is free."
    }
} catch {
    Write-Warning ("Could not check port {0}: {1}" -f $port, $_)
}

Write-Host "Starting ms-gateway (mvn -DskipTests spring-boot:run)..."
mvn -DskipTests spring-boot:run
