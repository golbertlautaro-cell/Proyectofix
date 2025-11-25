# MS-Solicitudes - Documentación de Reglas de Negocio

## 📋 Tabla de Contenidos
1. [Entidades y Campos](#entidades-y-campos)
2. [Reglas de Negocio](#reglas-de-negocio)
3. [Migraciones de Base de Datos](#migraciones-de-base-de-datos)
4. [Validaciones Implementadas](#validaciones-implementadas)
5. [DTOs y Mapeos](#dtos-y-mapeos)

---

## 🏗️ Entidades y Campos

### Entidad: **Contenedor**

Representa un contenedor logístico asociado a un cliente.

#### Campos Principales:
| Campo | Tipo | Descripción | Obligatorio |
|-------|------|-------------|-------------|
| `idContenedor` | Long | Identificador único | Sí (auto) |
| `cliente` | Cliente | Cliente propietario | Sí |
| `descripcion` | String | Descripción del contenedor | Sí |
| `tipo` | String | Tipo de contenedor (ej: DRY20) | No |
| `capacidadKg` | Double | Capacidad máxima en kg | Sí |

#### Campos de Estado y Ubicación:
| Campo | Tipo | Descripción | Valores Permitidos |
|-------|------|-------------|-------------------|
| `estado` | String | Estado actual del contenedor | `EN_ORIGEN`, `EN_TRANSITO`, `EN_DEPOSITO`, `ENTREGADO`, `DISPONIBLE` |
| `depositoActualId` | Long | ID del depósito donde se encuentra | null o ID válido |
| `pesoReal` | Double | Peso real actual en kg | Opcional |
| `volumenReal` | Double | Volumen real en m³ | Opcional |

---

### Entidad: **Tramo**

Representa un segmento de una ruta logística.

#### Campos Principales:
| Campo | Tipo | Descripción | Obligatorio |
|-------|------|-------------|-------------|
| `idTramo` | Long | Identificador único | Sí (auto) |
| `ruta` | Ruta | Ruta a la que pertenece | Sí |
| `origen` | String | Punto de origen | Sí |
| `destino` | String | Punto de destino | Sí |
| `dominioCamion` | String | Dominio del camión asignado | No |
| `estado` | EstadoTramo | Estado del tramo | Sí |

#### Campos de Ubicación (Origen):
| Campo | Tipo | Descripción | Uso |
|-------|------|-------------|-----|
| `origenDepositoId` | Long | ID del depósito de origen | Cuando el origen es un depósito |
| `origenDireccionLibre` | String | Dirección libre de origen | Cuando el origen no es un depósito |

#### Campos de Ubicación (Destino):
| Campo | Tipo | Descripción | Uso |
|-------|------|-------------|-----|
| `destinoDepositoId` | Long | ID del depósito de destino | Cuando el destino es un depósito |
| `destinoDireccionLibre` | String | Dirección libre de destino | Cuando el destino no es un depósito |

#### Campos de Costos y Tiempo:
| Campo | Tipo | Descripción |
|-------|------|-------------|
| `tiempoEstadiaHoras` | Double | Tiempo de estadía en horas |
| `costoEstadiaReal` | Double | Costo real de estadía |
| `costoReal` | Double | Costo total real del tramo |
| `costoAproximado` | Double | Costo estimado |
| `distanciaRealKm` | Double | Distancia real recorrida |
| `distanciaEstimadaKm` | Double | Distancia estimada |

---

### Entidad: **Solicitud**

Representa una solicitud de transporte logístico.

#### Campos Principales:
| Campo | Tipo | Descripción | Obligatorio |
|-------|------|-------------|-------------|
| `nroSolicitud` | Long | Número único de solicitud | Sí (auto) |
| `idContenedor` | Long | ID del contenedor | No |
| `idCliente` | Long | ID del cliente | No |
| `estado` | EstadoSolicitud | Estado de la solicitud | No |

#### Campos de Costos:
| Campo | Tipo | Descripción |
|-------|------|-------------|
| `costoEstimado` | Double | Costo estimado del transporte |
| `costoFinal` | Double | Costo final del transporte |
| `costoTotalEstimado` | Double | Costo total estimado (todas las rutas/tramos) |
| `costoTotalReal` | Double | Costo total real (todas las rutas/tramos) |

#### Campos de Tiempo:
| Campo | Tipo | Descripción |
|-------|------|-------------|
| `tiempoReal` | Double | Tiempo real de transporte en horas |
| `tiempoTotalEstimadoHoras` | Double | Tiempo total estimado en horas |
| `tiempoTotalRealHoras` | Double | Tiempo total real en horas |

#### Estados Disponibles (EstadoSolicitud):
- `BORRADOR` - Solicitud en borrador (no confirmada)
- `PROGRAMADA` - Solicitud programada
- `EN_TRANSITO` - En tránsito
- `ENTREGADA` - Entregada
- `CANCELADA` - Cancelada
- `COMPLETADA` - Completada

---

### Entidad: **Deposito** (ms-logistica)

Representa un depósito logístico.

#### Campos Principales:
| Campo | Tipo | Descripción | Obligatorio |
|-------|------|-------------|-------------|
| `idDeposito` | Long | Identificador único | Sí (auto) |
| `nombre` | String | Nombre del depósito | Sí |
| `latitud` | Double | Coordenada de latitud | No |
| `longitud` | Double | Coordenada de longitud | No |
| `direccion` | String | Dirección del depósito | No |

#### Campos de Tarifas:
| Campo | Tipo | Descripción |
|-------|------|-------------|
| `costoEstadiaDiario` | Double | Costo de estadía por día (obligatorio) |
| `tarifaEstadiaPorHora` | Double | Tarifa de estadía por hora |

---

## ⚖️ Reglas de Negocio

### 1. **Contenedor - Estados y Depósitos**

Las siguientes reglas se aplican automáticamente al crear o actualizar un contenedor:

#### Estado: `EN_ORIGEN`
- ✅ **Regla**: El contenedor NO puede tener `depositoActualId`
- ❌ **Violación**: Si `depositoActualId != null` → Error
- 📝 **Razón**: Un contenedor en origen aún no ha sido movido a ningún depósito

```java
// Ejemplo válido
Contenedor c = new Contenedor();
c.setEstado("EN_ORIGEN");
c.setDepositoActualId(null); // ✓ Correcto
```

#### Estado: `EN_TRANSITO`
- ✅ **Regla**: El contenedor NUNCA debe tener `depositoActualId`
- ❌ **Violación**: Si `depositoActualId != null` → Error
- 📝 **Razón**: Un contenedor en tránsito no está en ningún depósito

```java
// Ejemplo válido
Contenedor c = new Contenedor();
c.setEstado("EN_TRANSITO");
c.setDepositoActualId(null); // ✓ Correcto
```

#### Estado: `EN_DEPOSITO`
- ✅ **Regla**: El contenedor DEBE tener `depositoActualId` (obligatorio)
- ❌ **Violación**: Si `depositoActualId == null` → Error
- 📝 **Razón**: Un contenedor en depósito debe estar ubicado en un depósito específico

```java
// Ejemplo válido
Contenedor c = new Contenedor();
c.setEstado("EN_DEPOSITO");
c.setDepositoActualId(5L); // ✓ Correcto (ID válido)
```

#### Estado: `ENTREGADO`
- ✅ **Regla**: El contenedor NO puede tener `depositoActualId`
- ❌ **Violación**: Si `depositoActualId != null` → Error
- 📝 **Razón**: Un contenedor entregado ya no está en ningún depósito

```java
// Ejemplo válido
Contenedor c = new Contenedor();
c.setEstado("ENTREGADO");
c.setDepositoActualId(null); // ✓ Correcto
```

#### Estado: `DISPONIBLE`
- ✅ **Regla**: Sin restricciones sobre `depositoActualId`
- 📝 **Razón**: Estado inicial/genérico, puede o no estar en depósito

---

### 2. **Tramo - Origen y Destino (Validaciones Estrictas)**

**REGLA FUNDAMENTAL**: Un tramo DEBE tener EXACTAMENTE UNA forma válida de especificar origen y destino.

#### Opciones Válidas para Origen:

**Opción A: Depósito**
- ✅ `origenDepositoId` tiene valor (not null)
- ✅ `origenDireccionLibre` es null o vacío
- 📝 Ejemplo: `origenDepositoId = 3`, `origenDireccionLibre = null`

**Opción B: Dirección Libre**
- ✅ `origenDepositoId` es null
- ✅ `origenDireccionLibre` tiene valor (not empty)
- 📝 Ejemplo: `origenDepositoId = null`, `origenDireccionLibre = "Calle 123"`

**❌ CASOS INVÁLIDOS (Error 422 - Unprocessable Entity):**

1. **Ambos null**:
   - `origenDepositoId = null` Y `origenDireccionLibre = null`
   - Error: "El origen debe especificarse mediante depositoId O direccionLibre. Ambos son null."

2. **Ambos con valor**:
   - `origenDepositoId = 3` Y `origenDireccionLibre = "Calle 123"`
   - Error: "El origen solo puede tener UNA forma: depositoId O direccionLibre, no ambos."

#### Opciones Válidas para Destino:

Las mismas reglas aplican para destino con `destinoDepositoId` y `destinoDireccionLibre`.

#### Campo Orden (Obligatorio):

- ✅ El campo `orden` es **obligatorio** (NOT NULL en BD)
- 🔢 Define la secuencia de tramos en una ruta (1, 2, 3...)
- 🤖 **Auto-asignación**: Si no se proporciona al crear, se asigna automáticamente el siguiente número disponible en la ruta
- 📝 Ejemplo: Primera inserción → `orden = 1`, segunda → `orden = 2`

**Validación implementada en**: `TramoService.validarOrigenDestino()`

**Invocada automáticamente en**: `crearTramoEnRuta()`

---

### 3. **Tramo - Cálculo de Costos**

Los tramos calculan costos de estadía con la siguiente prioridad:

#### Cálculo de Costo de Estadía:
- Si `destinoDepositoId` existe → Se usa para calcular costo de estadía
- Si no, se usa `origenDepositoId`
- Si ninguno existe → Se usa tarifa por defecto

---

## 🗄️ Migraciones de Base de Datos

### V2: Actualización de Contenedores
**Archivo**: `V2__add_contenedores_columns.sql`

```sql
ALTER TABLE contenedores
  ADD COLUMN IF NOT EXISTS peso_real double precision,
  ADD COLUMN IF NOT EXISTS volumen_real double precision,
  ADD COLUMN IF NOT EXISTS deposito_actual_id bigint;
```

**Campos Agregados**:
- `peso_real`: Peso real del contenedor en kg
- `volumen_real`: Volumen real del contenedor en m³
- `deposito_actual_id`: ID del depósito actual donde se encuentra

---

### V3: Actualización de Tramos
**Archivo**: `V3__update_tramo_depositos.sql`

```sql
-- Elimina columna antigua
ALTER TABLE tramos DROP COLUMN IF EXISTS deposito_id;

-- Agrega nuevas columnas
ALTER TABLE tramos
  ADD COLUMN IF NOT EXISTS origen_deposito_id bigint,
  ADD COLUMN IF NOT EXISTS destino_deposito_id bigint,
  ADD COLUMN IF NOT EXISTS origen_direccion_libre varchar(200),
  ADD COLUMN IF NOT EXISTS destino_direccion_libre varchar(200),
  ADD COLUMN IF NOT EXISTS tiempo_estadia_horas double precision,
  ADD COLUMN IF NOT EXISTS costo_estadia_real double precision;
```

**Cambios Realizados**:
1. **Eliminado**: `deposito_id` (campo único)
2. **Agregados**:
   - `origen_deposito_id`: ID del depósito de origen (opcional)
   - `destino_deposito_id`: ID del depósito de destino (opcional)
   - `origen_direccion_libre`: Dirección libre de origen (alternativa)
   - `destino_direccion_libre`: Dirección libre de destino (alternativa)
   - `tiempo_estadia_horas`: Tiempo de estadía en horas
   - `costo_estadia_real`: Costo real de estadía en depósito

---

### V4: Campo Orden en Tramos
**Archivo**: `V4__add_tramo_orden.sql`

```sql
ALTER TABLE tramos ADD COLUMN orden integer DEFAULT 1 NOT NULL;
```

**Campo Agregado**:
- `orden`: Orden del tramo dentro de la ruta (obligatorio)

---

### V5: Costos y Tiempos Totales en Solicitudes
**Archivo**: `V5__add_solicitud_costos_tiempos_totales.sql`

```sql
ALTER TABLE solicitudes
  ADD COLUMN IF NOT EXISTS costo_total_estimado double precision,
  ADD COLUMN IF NOT EXISTS costo_total_real double precision,
  ADD COLUMN IF NOT EXISTS tiempo_total_estimado_horas double precision,
  ADD COLUMN IF NOT EXISTS tiempo_total_real_horas double precision;
```

**Campos Agregados**:
- `costo_total_estimado`: Costo total estimado de todas las rutas/tramos
- `costo_total_real`: Costo total real de todas las rutas/tramos
- `tiempo_total_estimado_horas`: Tiempo total estimado en horas
- `tiempo_total_real_horas`: Tiempo total real en horas

---

### Migración Manual: Tarifa por Hora en Depósitos (ms-logistica)
**Archivo**: `ms-logistica/add_deposito_tarifa_estadia_por_hora.sql`
**Base de Datos**: `logistica_db`

```sql
ALTER TABLE depositos
  ADD COLUMN IF NOT EXISTS tarifa_estadia_por_hora double precision;

-- Calcular valor inicial basado en costo_estadia_diario
UPDATE depositos
SET tarifa_estadia_por_hora = costo_estadia_diario / 24.0
WHERE tarifa_estadia_por_hora IS NULL AND costo_estadia_diario IS NOT NULL;
```

**Campo Agregado**:
- `tarifa_estadia_por_hora`: Tarifa de estadía por hora

**Nota**: Esta migración debe aplicarse manualmente en la base de datos `logistica_db` ya que ms-logistica no tiene Flyway configurado.

---

## ✅ Validaciones Implementadas

### ContenedorService

#### Método: `validarReglasDeNegocio(Contenedor contenedor)`

Valida las reglas de negocio del contenedor antes de persistir:

```java
private void validarReglasDeNegocio(Contenedor contenedor) {
    String estado = contenedor.getEstado();
    Long depositoId = contenedor.getDepositoActualId();

    switch (estado.toUpperCase()) {
        case "EN_ORIGEN":
        case "EN_TRANSITO":
        case "ENTREGADO":
            // depositoActualId DEBE ser null
            if (depositoId != null) {
                throw new IllegalArgumentException(...);
            }
            break;
            
        case "EN_DEPOSITO":
            // depositoActualId es OBLIGATORIO
            if (depositoId == null) {
                throw new IllegalArgumentException(...);
            }
            break;
    }
}
```

**Invocación**: Se ejecuta automáticamente en:
- `crearContenedor()`
- `actualizarContenedor()`

---

### TramoService

#### Método: `crearTramoEnRuta(Long rutaId, TramoCreateDto dto)`

Mapea y valida los campos de origen/destino del DTO al crear un tramo:

```java
// Setear depósitos si vienen en el DTO
if (dto.getOrigenDepositoId() != null) {
    tramo.setOrigenDepositoId(dto.getOrigenDepositoId());
}
if (dto.getDestinoDepositoId() != null) {
    tramo.setDestinoDepositoId(dto.getDestinoDepositoId());
}

// Setear direcciones libres si vienen en el DTO
if (dto.getOrigenDireccionLibre() != null) {
    tramo.setOrigenDireccionLibre(dto.getOrigenDireccionLibre());
}
if (dto.getDestinoDireccionLibre() != null) {
    tramo.setDestinoDireccionLibre(dto.getDestinoDireccionLibre());
}
```

---

## 📦 DTOs y Mapeos

### ContenedorCreateDto

```java
{
    "descripcion": "Contenedor de 20 pies",
    "tipo": "DRY20",
    "capacidadKg": 25000,
    "estado": "DISPONIBLE",           // Opcional (default: DISPONIBLE)
    "pesoReal": 1200.5,                // Opcional
    "volumenReal": 12.3,               // Opcional
    "depositoActualId": 5              // Opcional (validado según estado)
}
```

### ContenedorUpdateDto

```java
{
    "descripcion": "...",              // Opcional
    "tipo": "...",                     // Opcional
    "capacidadKg": 25000,              // Opcional
    "estado": "EN_DEPOSITO",           // Opcional (dispara validación)
    "pesoReal": 1500.0,                // Opcional
    "volumenReal": 15.0,               // Opcional
    "depositoActualId": 3              // Opcional (validado según estado)
}
```

### TramoCreateDto

```java
{
    "origen": "Buenos Aires",
    "destino": "Rosario",
    "dominioCamion": "ABC123",
    "origenDepositoId": 1,             // Opcional
    "destinoDepositoId": 5,            // Opcional
    "origenDireccionLibre": "Calle 1", // Opcional
    "destinoDireccionLibre": "Av 2",   // Opcional
    "tiempoEstadiaHoras": 24.0,        // Opcional
    "costoEstadiaReal": 150.0,         // Opcional
    "fechaHoraInicioEstimada": "...",
    "fechaHoraFinEstimada": "..."
}
```

---

## 🔧 Configuración de Flyway

### application.yml

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
```

### Dependencia Maven

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

---

## 📝 Ejemplos de Uso

### Ejemplo 1: Crear Contenedor en Depósito

```java
ContenedorCreateDto dto = ContenedorCreateDto.builder()
    .descripcion("Contenedor refrigerado")
    .tipo("REEFER20")
    .capacidadKg(20000.0)
    .estado("EN_DEPOSITO")
    .depositoActualId(3L)  // Obligatorio para estado EN_DEPOSITO
    .pesoReal(1800.0)
    .volumenReal(25.0)
    .build();

Contenedor contenedor = contenedorService.crearContenedor(clienteId, dto);
// ✓ Se valida y guarda correctamente
```

### Ejemplo 2: Intentar Crear Contenedor Inválido

```java
ContenedorCreateDto dto = ContenedorCreateDto.builder()
    .descripcion("Contenedor en tránsito")
    .tipo("DRY40")
    .capacidadKg(30000.0)
    .estado("EN_TRANSITO")
    .depositoActualId(5L)  // ❌ ERROR: EN_TRANSITO no puede tener depositoActualId
    .build();

// Lanza: IllegalArgumentException
// "Un contenedor EN_TRANSITO no puede tener depositoActualId. Debe ser null."
```

### Ejemplo 3: Crear Tramo con Depósito y Dirección Libre

```java
TramoCreateDto dto = TramoCreateDto.builder()
    .origen("Buenos Aires")
    .destino("Córdoba")
    .origenDepositoId(1L)
    .destinoDepositoId(5L)
    .origenDireccionLibre("Depósito Central BA")
    .destinoDireccionLibre("Depósito Norte CBA")
    .tiempoEstadiaHoras(48.0)
    .costoEstadiaReal(300.0)
    .build();

Tramo tramo = tramoService.crearTramoEnRuta(rutaId, dto);
// ✓ Se guardan todos los campos
```

---

## 🚀 Próximos Pasos Recomendados

1. **Testing**: Crear tests unitarios para validaciones de negocio
2. **Documentación API**: Actualizar Swagger con ejemplos de las reglas
3. **Logs**: Agregar logging detallado en validaciones para auditoría
4. **Métricas**: Implementar métricas para trackear violaciones de reglas
5. **Enum**: Considerar crear enum `EstadoContenedor` para type-safety

---

## 📞 Contacto y Soporte

Para dudas o cambios en las reglas de negocio, contactar al equipo de desarrollo.

**Última actualización**: 2025-11-24
**Versión del documento**: 1.0
