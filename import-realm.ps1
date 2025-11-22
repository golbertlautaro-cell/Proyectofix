# Script para importar realm en Keycloak

# 1. Obtener token admin
Write-Host "Obteniendo token admin..."
$tokenResponse = Invoke-WebRequest `
    -Uri "http://localhost:8090/realms/master/protocol/openid-connect/token" `
    -Method POST `
    -Body "grant_type=password&username=admin&password=admin123&client_id=admin-cli" `
    -ContentType "application/x-www-form-urlencoded" `
    -UseBasicParsing

$token = ($tokenResponse.Content | ConvertFrom-Json).access_token
Write-Host "Token obtenido"

# 2. Leer el archivo de realm export
$realmJson = Get-Content -Path ".\keycloak\realm-export.json" -Raw

# 3. Importar el realm
Write-Host "Importando realm..."
$importResponse = Invoke-WebRequest `
    -Uri "http://localhost:8090/admin/realms" `
    -Method POST `
    -Body $realmJson `
    -ContentType "application/json" `
    -Headers @{"Authorization" = "Bearer $token"} `
    -UseBasicParsing

Write-Host "Realm tpi-realm importado"
Write-Host "Setup completado!"
