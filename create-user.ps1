#!/usr/bin/env powershell

# Obtener token admin
$tokenResp = Invoke-WebRequest -Uri "http://localhost:8090/realms/master/protocol/openid-connect/token" `
    -Method POST `
    -Body "grant_type=password&username=admin&password=admin123&client_id=admin-cli" `
    -ContentType "application/x-www-form-urlencoded" `
    -UseBasicParsing

$adminToken = ($tokenResp.Content | ConvertFrom-Json).access_token
Write-Host "Token admin obtenido"

# Crear usuario cliente1
$userBody = @{
    username = "cliente1"
    enabled = $true
    credentials = @(
        @{
            type = "password"
            value = "password123"
            temporary = $false
        }
    )
} | ConvertTo-Json

try {
    Invoke-WebRequest -Uri "http://localhost:8090/admin/realms/tpi-realm/users" `
        -Method POST `
        -Body $userBody `
        -ContentType "application/json" `
        -Headers @{"Authorization" = "Bearer $adminToken"} `
        -UseBasicParsing
    Write-Host "Usuario cliente1 creado"
} catch {
    Write-Host "Cliente1 ya existe o error"
}

# Obtener token para cliente1
Start-Sleep -Seconds 2
$tokenResp2 = Invoke-WebRequest -Uri "http://localhost:8090/realms/tpi-realm/protocol/openid-connect/token" `
    -Method POST `
    -Body "grant_type=password&username=cliente1&password=password123&client_id=tpi-backend" `
    -ContentType "application/x-www-form-urlencoded" `
    -UseBasicParsing

$userToken = ($tokenResp2.Content | ConvertFrom-Json).access_token
Write-Host "Token de cliente1 obtenido"
$userToken | Set-Content -Path d:\token.txt -Force
Write-Host "Token guardado en d:\token.txt"
