Write-Host "====================================" -ForegroundColor Cyan
Write-Host " SOLUCION FINAL - FLYWAY V4 ROTO" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan
Write-Host ""

Set-Location C:\Users\Usuario-\IdeaProjects\Proyectofix

Write-Host "[1/5] Reparando Flyway - eliminando V4 fallido..." -ForegroundColor Yellow
docker exec -it tpi-postgres psql -U postgres -d solicitudes_db -c "DELETE FROM flyway_schema_history WHERE version = '4' AND success = false;"
Write-Host "V4 fallido eliminado" -ForegroundColor Green
Write-Host ""

Write-Host "[2/5] Reconstruyendo imagen Docker con V4 corregido..." -ForegroundColor Yellow
docker-compose build ms-solicitudes
Write-Host "Imagen reconstruida" -ForegroundColor Green
Write-Host ""

Write-Host "[3/5] Reiniciando ms-solicitudes..." -ForegroundColor Yellow
docker-compose stop ms-solicitudes
docker-compose rm -f ms-solicitudes
docker-compose up -d ms-solicitudes
Write-Host "Contenedor reiniciado" -ForegroundColor Green
Write-Host ""

Write-Host "[4/5] Esperando 60 segundos para Flyway..." -ForegroundColor Yellow
for ($i = 60; $i -gt 0; $i--) {
    Write-Progress -Activity "Esperando Flyway V1-V5" -Status "$i segundos" -PercentComplete ((60-$i)/60*100)
    Start-Sleep 1
}
Write-Progress -Completed -Activity "Esperando Flyway V1-V5"
Write-Host "Espera completada" -ForegroundColor Green
Write-Host ""

Write-Host "[5/5] Verificando migraciones Flyway..." -ForegroundColor Yellow
docker exec -it tpi-postgres psql -U postgres -d solicitudes_db -c "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;"
Write-Host ""

Write-Host "====================================" -ForegroundColor Cyan
Write-Host " VERIFICACION FINAL" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Estado contenedor:" -ForegroundColor Yellow
docker ps | Select-String "ms-solicitudes"
Write-Host ""

Write-Host "Verificando logs de inicio..." -ForegroundColor Yellow
docker-compose logs ms-solicitudes | Select-String "Started MsSolicitudesApplication"
Write-Host ""

Write-Host "Probando puerto 8080..." -ForegroundColor Yellow
$port = netstat -ano | Select-String ":8080" | Select-Object -First 1
if ($port) {
    Write-Host "Puerto 8080 ACTIVO" -ForegroundColor Green
    Write-Host ""
    Write-Host "Abriendo Swagger..." -ForegroundColor Yellow
    Start-Sleep 3
    Start-Process "http://localhost:8080/swagger-ui/index.html"
} else {
    Write-Host "Puerto 8080 NO activo - revisar logs:" -ForegroundColor Red
    docker-compose logs --tail=30 ms-solicitudes
}
Write-Host ""

Write-Host "====================================" -ForegroundColor Cyan
Write-Host " SWAGGER DEBERIA FUNCIONAR AHORA" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "URL: http://localhost:8080/swagger-ui/index.html" -ForegroundColor White
Write-Host ""

