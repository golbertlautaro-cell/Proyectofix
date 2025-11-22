# ms-logistica

Run scripts and development notes for ms-logistica.

Quick-run (background jar):
```powershell
cd D:\Users\Usuario\Desktop\backend1\ms-logistica
powershell -ExecutionPolicy Bypass -File .\scripts\run-jar-ms-logistica.ps1
```
This will package the app (`mvn -DskipTests package`) and start the resulting jar in background. Logs are written to `./logs/ms-logistica.out.log` and `./logs/ms-logistica.err.log`.

Run with Maven (interactive):
```powershell
cd D:\Users\Usuario\Desktop\backend1\ms-logistica
mvn -DskipTests spring-boot:run
```
Note: running `mvn spring-boot:run` in the foreground ties the process to your terminal. Use the jar-run script to run the service independently while you run other Maven commands.

Ports and config
- Default port: 8081 (set in `src/main/resources/application.yml`).
- Database: the app expects PostgreSQL available at `jdbc:postgresql://localhost:5432/logistica_db` unless overridden with environment variables `DB_USERNAME`, `DB_PASSWORD`, or full `SPRING_DATASOURCE_URL`.

Stopping the service
- Find the Java process and stop it:
```powershell
Get-Process -Name java | Select-Object Id, ProcessName, CPU, WS
Stop-Process -Id <PID> -Force
```

If you use Docker Compose instead, see the repo-level `docker-compose.yml` for a containerized setup.
