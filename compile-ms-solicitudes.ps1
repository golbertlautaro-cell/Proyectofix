#!/usr/bin/env pwsh
Set-Location "c:\Users\juanc\Desktop\backend1\ms-solicitudes"
mvn clean package -DskipTests | Tee-Object -FilePath build.log
Get-Content build.log | Select-Object -Last 30

