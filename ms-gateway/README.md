# ms-gateway

Run scripts and development notes for ms-gateway.

Quick-run (background jar):
```powershell
cd D:\Users\Usuario\Desktop\backend1\ms-gateway
powershell -ExecutionPolicy Bypass -File .\scripts\run-jar-ms-gateway.ps1
```
This will package the app (`mvn -DskipTests package`) and start the resulting jar in background. Logs are written to `./logs/ms-gateway.out.log` and `./logs/ms-gateway.err.log`.

Run with Maven (interactive):
```powershell
cd D:\Users\Usuario\Desktop\backend1\ms-gateway
mvn -DskipTests spring-boot:run
```

Ports and config
- Default port: 8080 (set in `src/main/resources/application.yml`).
- The gateway forwards `/api/solicitudes/**` to ms-solicitudes (port 8082) and `/api/...` to ms-logistica (port 8081) as configured.

Stopping the service
```powershell
Get-Process -Name java | Select-Object Id, ProcessName, CPU, WS
Stop-Process -Id <PID> -Force
```

If you use Docker Compose, see the repo-level `docker-compose.yml` for containerized setup.
