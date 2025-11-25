# ms-solicitudes

Microservicio Spring Boot 3.3.x (Java 21) para gestión de solicitudes.

## 📚 Documentación

- **[Reglas de Negocio](./REGLAS_DE_NEGOCIO.md)** - Documentación completa de validaciones, estados y lógica de negocio
- **[API Docs](./api-docs.json)** - Especificación OpenAPI/Swagger

## 🚀 Quick Start

### Run scripts and development notes for ms-solicitudes.

Quick-run (background jar):
```powershell
cd C:\Users\Usuario-\IdeaProjects\Proyectofix\ms-solicitudes
powershell -ExecutionPolicy Bypass -File .\scripts\run-jar-ms-solicitudes.ps1
```
This will package the app (`mvn -DskipTests package`) and start the resulting jar in background. Logs are written to `./logs/ms-solicitudes.out.log` and `./logs/ms-solicitudes.err.log`.

Run with Maven (interactive):
```powershell
cd C:\Users\Usuario-\IdeaProjects\Proyectofix\ms-solicitudes
mvn -DskipTests spring-boot:run
```

## ⚙️ Configuration

### Ports and config
- Default port: 8082 (set in `src/main/resources/application.yml`).
- Database: the app expects PostgreSQL available at `jdbc:postgresql://localhost:5432/solicitudes_db` unless overridden by environment variables.

### Database Migrations (Flyway)
Este proyecto usa **Flyway** para versionado de esquema de base de datos.

**Migraciones Actuales**:
- `V2__add_contenedores_columns.sql` - Agrega campos de estado y ubicación a contenedores
- `V3__update_tramo_depositos.sql` - Actualiza tramos con origen/destino flexibles

Las migraciones se aplican automáticamente al iniciar la aplicación.

## 🛑 Stopping the service
```powershell
Get-Process -Name java | Select-Object Id, ProcessName, CPU, WS
Stop-Process -Id <PID> -Force
```

## 🔐 Security & Keycloak
- The repo includes a Keycloak realm export at `keycloak/realm-export.json` with test users (e.g. `cliente1` / `password123`) and a public client `tpi-backend`.
- To obtain a token for E2E tests, use the token endpoint:
  `http://localhost:8090/realms/tpi-realm/protocol/openid-connect/token`

**Nota**: Actualmente la seguridad está en modo testing (permitAll) para facilitar desarrollo.

## 🐳 Docker

If you use Docker Compose, see the repo-level `docker-compose.yml` for containerized setup.

## 🛠️ Tech Stack

- Spring Boot 3.3.5
- Java 21
- Spring Data JPA
- Spring Security (OAuth2 Resource Server)
- PostgreSQL
- Flyway (Database Migrations)
- Lombok
- Springdoc OpenAPI (Swagger)
- WebFlux (para comunicación entre microservicios)

## 📋 Reglas de Negocio Principales

### Contenedor - Estados
- **EN_ORIGEN**: No puede tener `depositoActualId`
- **EN_TRANSITO**: No puede tener `depositoActualId`
- **EN_DEPOSITO**: Debe tener `depositoActualId` (obligatorio)
- **ENTREGADO**: No puede tener `depositoActualId`
- **DISPONIBLE**: Sin restricciones

Ver documentación completa en [REGLAS_DE_NEGOCIO.md](./REGLAS_DE_NEGOCIO.md)

## 🧪 Testing

```powershell
# Run tests
mvn test

# Run with coverage
mvn clean verify
```

## 📝 Endpoints Principales

- `GET /api/solicitudes` - Listar solicitudes
- `POST /api/solicitudes` - Crear solicitud
- `GET /api/clientes/{id}/contenedores` - Listar contenedores de un cliente
- `POST /api/clientes/{id}/contenedores` - Crear contenedor
- `GET /api/rutas` - Listar rutas
- `POST /api/rutas/solicitudes/{id}` - Crear ruta alternativa

**Documentación completa**: http://localhost:8082/swagger-ui.html

---

**Última actualización**: 2025-11-24
