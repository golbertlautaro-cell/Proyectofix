# Build and run ms-gateway as a background Java process (independent of Maven interactive run)
param(
    [string]$mvnArgs = "-DskipTests package",
    [string]$javaOpts = "-Duser.timezone=UTC -Xms256m -Xmx512m"
)

Write-Host "Building ms-gateway with: mvn $mvnArgs"
$mvn = Start-Process -FilePath mvn -ArgumentList $mvnArgs -NoNewWindow -Wait -PassThru
if ($mvn.ExitCode -ne 0) {
    Write-Error "Maven build failed with exit code $($mvn.ExitCode). Aborting run."
    exit $mvn.ExitCode
}

# find jar
$jar = Get-ChildItem -Path .\target -Filter "*-SNAPSHOT.jar" | Select-Object -First 1
if ($null -eq $jar) {
    Write-Error "Jar not found in target/. Did build succeed?"
    exit 1
}

 $logFileOut = Join-Path -Path .\logs -ChildPath "ms-gateway.out.log"
 $logFileErr = Join-Path -Path .\logs -ChildPath "ms-gateway.err.log"
if (!(Test-Path -Path .\logs)) { New-Item -ItemType Directory -Path .\logs | Out-Null }

Write-Host "Starting jar $($jar.FullName) in background. Logs -> $logFile"
Start-Process -FilePath java -ArgumentList "$javaOpts -jar `"$($jar.FullName)`"" -RedirectStandardOutput $logFileOut -RedirectStandardError $logFileErr -WindowStyle Hidden -PassThru
Write-Host "Started ms-gateway (check logs with Get-Content -Tail 50 .\logs\ms-gateway.out.log -Wait)"