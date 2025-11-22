# ms-solicitudes

Microservicio Spring Boot 3.3.x (Java 21) para gestión de solicitudes.

## ms-solicitudes

Run scripts and development notes for ms-solicitudes.

Quick-run (background jar):
```powershell
cd D:\Users\Usuario\Desktop\backend1\ms-solicitudes
powershell -ExecutionPolicy Bypass -File .\scripts\run-jar-ms-solicitudes.ps1
```
This will package the app (`mvn -DskipTests package`) and start the resulting jar in background. Logs are written to `./logs/ms-solicitudes.out.log` and `./logs/ms-solicitudes.err.log`.

Run with Maven (interactive):
```powershell
cd D:\Users\Usuario\Desktop\backend1\ms-solicitudes
mvn -DskipTests spring-boot:run
```

Ports and config
- Default port: 8082 (set in `src/main/resources/application.properties`).
- Database: the app expects PostgreSQL available at `jdbc:postgresql://localhost:5432/solicitudes_db` unless overridden by environment variables.

Stopping the service
```powershell
Get-Process -Name java | Select-Object Id, ProcessName, CPU, WS
Stop-Process -Id <PID> -Force
```

Security & Keycloak
- The repo includes a Keycloak realm export at `keycloak/realm-export.json` with test users (e.g. `cliente1` / `password123`) and a public client `tpi-backend`.
- To obtain a token for E2E tests, use the token endpoint:
  `http://localhost:8090/realms/tpi-realm/protocol/openid-connect/token`

If you use Docker Compose, see the repo-level `docker-compose.yml` for containerized setup.
- Spring Security (sin config inicial)
- Lombok
- PostgreSQL Driver
