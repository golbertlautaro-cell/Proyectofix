#!/usr/bin/env pwsh
Set-Location "c:\Users\juanc\Desktop\backend1\ms-solicitudes"
Write-Host "Removiendo target..."
Remove-Item -Recurse -Force target -ErrorAction SilentlyContinue
Write-Host "Compilando ms-solicitudes..."
mvn clean package -DskipTests
Write-Host "Compilación finalizada"
Get-ChildItem target\*.jar -ErrorAction SilentlyContinue | ForEach-Object { Write-Host "JAR creado: $_" }

