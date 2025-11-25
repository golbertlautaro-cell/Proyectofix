-- V1__init.sql
-- Migración inicial: Creación del esquema completo de ms-solicitudes
-- Este script crea todas las tablas base del sistema de solicitudes de transporte
-- Compatible con PostgreSQL

-- =====================================================
-- Tabla: clientes
-- Descripción: Almacena información de los clientes que solicitan servicios de transporte
-- =====================================================
CREATE TABLE IF NOT EXISTS clientes (
    id_cliente BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    apellido VARCHAR(120),
    email VARCHAR(180),
    telefono VARCHAR(30),
    direccion VARCHAR(200)
);

COMMENT ON TABLE clientes IS 'Clientes que solicitan servicios de transporte';
COMMENT ON COLUMN clientes.id_cliente IS 'Identificador único del cliente';
COMMENT ON COLUMN clientes.nombre IS 'Nombre del cliente (obligatorio)';
COMMENT ON COLUMN clientes.email IS 'Correo electrónico del cliente';

-- =====================================================
-- Tabla: contenedores
-- Descripción: Contenedores de carga propiedad de los clientes
-- =====================================================
CREATE TABLE IF NOT EXISTS contenedores (
    id_contenedor BIGSERIAL PRIMARY KEY,
    id_cliente BIGINT NOT NULL,
    descripcion VARCHAR(100) NOT NULL,
    tipo VARCHAR(50),
    capacidad_kg DOUBLE PRECISION,
    estado VARCHAR(30),
    CONSTRAINT fk_contenedores_cliente FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente) ON DELETE CASCADE
);

COMMENT ON TABLE contenedores IS 'Contenedores de carga asociados a clientes';
COMMENT ON COLUMN contenedores.id_contenedor IS 'Identificador único del contenedor';
COMMENT ON COLUMN contenedores.id_cliente IS 'Cliente propietario del contenedor';
COMMENT ON COLUMN contenedores.descripcion IS 'Descripción del contenedor';
COMMENT ON COLUMN contenedores.tipo IS 'Tipo de contenedor (ej: DRY20, REEFER40)';
COMMENT ON COLUMN contenedores.capacidad_kg IS 'Capacidad máxima en kilogramos';
COMMENT ON COLUMN contenedores.estado IS 'Estado del contenedor: EN_ORIGEN, EN_TRANSITO, EN_DEPOSITO, ENTREGADO, DISPONIBLE';

-- Índices para contenedores
CREATE INDEX IF NOT EXISTS idx_contenedores_cliente ON contenedores(id_cliente);
CREATE INDEX IF NOT EXISTS idx_contenedores_estado ON contenedores(estado);

-- =====================================================
-- Tabla: solicitudes
-- Descripción: Solicitudes de transporte de contenedores
-- =====================================================
CREATE TABLE IF NOT EXISTS solicitudes (
    nro_solicitud BIGSERIAL PRIMARY KEY,
    id_contenedor BIGINT,
    id_cliente BIGINT,
    estado VARCHAR(30),
    costo_estimado DOUBLE PRECISION,
    costo_final DOUBLE PRECISION,
    tiempo_real DOUBLE PRECISION,
    fecha_creacion TIMESTAMP,
    fecha_actualizacion TIMESTAMP
);

COMMENT ON TABLE solicitudes IS 'Solicitudes de transporte de contenedores';
COMMENT ON COLUMN solicitudes.nro_solicitud IS 'Número único de solicitud';
COMMENT ON COLUMN solicitudes.id_contenedor IS 'Contenedor a transportar';
COMMENT ON COLUMN solicitudes.id_cliente IS 'Cliente que solicita el transporte';
COMMENT ON COLUMN solicitudes.estado IS 'Estado de la solicitud: BORRADOR, PROGRAMADA, EN_TRANSITO, ENTREGADA';
COMMENT ON COLUMN solicitudes.costo_estimado IS 'Costo estimado del transporte';
COMMENT ON COLUMN solicitudes.costo_final IS 'Costo final del transporte';
COMMENT ON COLUMN solicitudes.tiempo_real IS 'Tiempo real de transporte en horas';
COMMENT ON COLUMN solicitudes.fecha_creacion IS 'Fecha de creación de la solicitud';
COMMENT ON COLUMN solicitudes.fecha_actualizacion IS 'Fecha de última actualización';

-- Índices para solicitudes
CREATE INDEX IF NOT EXISTS idx_solicitudes_contenedor ON solicitudes(id_contenedor);
CREATE INDEX IF NOT EXISTS idx_solicitudes_cliente ON solicitudes(id_cliente);
CREATE INDEX IF NOT EXISTS idx_solicitudes_estado ON solicitudes(estado);
CREATE INDEX IF NOT EXISTS idx_solicitudes_fecha_creacion ON solicitudes(fecha_creacion);

-- =====================================================
-- Tabla: rutas
-- Descripción: Rutas alternativas para cada solicitud
-- =====================================================
CREATE TABLE IF NOT EXISTS rutas (
    id_ruta BIGSERIAL PRIMARY KEY,
    nro_solicitud BIGINT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(500),
    estado VARCHAR(30),
    distancia_total_km DOUBLE PRECISION,
    duracion_estimada_horas DOUBLE PRECISION,
    costo_estimado DOUBLE PRECISION,
    costo_real DOUBLE PRECISION,
    es_ruta_seleccionada BOOLEAN DEFAULT FALSE,
    fecha_creacion TIMESTAMP,
    fecha_actualizacion TIMESTAMP,
    CONSTRAINT fk_rutas_solicitud FOREIGN KEY (nro_solicitud) REFERENCES solicitudes(nro_solicitud) ON DELETE CASCADE
);

COMMENT ON TABLE rutas IS 'Rutas alternativas para transportar un contenedor';
COMMENT ON COLUMN rutas.id_ruta IS 'Identificador único de la ruta';
COMMENT ON COLUMN rutas.nro_solicitud IS 'Solicitud a la que pertenece la ruta';
COMMENT ON COLUMN rutas.nombre IS 'Nombre descriptivo de la ruta';
COMMENT ON COLUMN rutas.descripcion IS 'Descripción detallada de la ruta';
COMMENT ON COLUMN rutas.estado IS 'Estado de la ruta: PENDIENTE, EJECUTANDOSE, COMPLETADA';
COMMENT ON COLUMN rutas.distancia_total_km IS 'Distancia total de la ruta en kilómetros (calculada)';
COMMENT ON COLUMN rutas.duracion_estimada_horas IS 'Duración estimada en horas (calculada)';
COMMENT ON COLUMN rutas.costo_estimado IS 'Costo estimado de la ruta (calculado)';
COMMENT ON COLUMN rutas.costo_real IS 'Costo real de la ruta (calculado al finalizar)';
COMMENT ON COLUMN rutas.es_ruta_seleccionada IS 'Indica si esta es la ruta seleccionada para ejecutar';
COMMENT ON COLUMN rutas.fecha_creacion IS 'Fecha de creación de la ruta';
COMMENT ON COLUMN rutas.fecha_actualizacion IS 'Fecha de última actualización';

-- Índices para rutas
CREATE INDEX IF NOT EXISTS idx_rutas_solicitud ON rutas(nro_solicitud);
CREATE INDEX IF NOT EXISTS idx_rutas_estado ON rutas(estado);
CREATE INDEX IF NOT EXISTS idx_rutas_seleccionada ON rutas(es_ruta_seleccionada) WHERE es_ruta_seleccionada = TRUE;

-- =====================================================
-- Tabla: tramos
-- Descripción: Tramos que componen cada ruta
-- =====================================================
CREATE TABLE IF NOT EXISTS tramos (
    id_tramo BIGSERIAL PRIMARY KEY,
    id_ruta BIGINT NOT NULL,
    origen VARCHAR(100),
    destino VARCHAR(100),
    dominio_camion VARCHAR(20),
    estado VARCHAR(30),
    fecha_hora_inicio_real TIMESTAMP,
    fecha_hora_fin_real TIMESTAMP,
    odometro_inicial DOUBLE PRECISION,
    odometro_final DOUBLE PRECISION,
    costo_real DOUBLE PRECISION,
    tiempo_real DOUBLE PRECISION,
    costo_aproximado DOUBLE PRECISION,
    fecha_hora_inicio_estimada TIMESTAMP,
    fecha_hora_fin_estimada TIMESTAMP,
    distancia_estimada_km DOUBLE PRECISION,
    distancia_real_km DOUBLE PRECISION,
    CONSTRAINT fk_tramos_ruta FOREIGN KEY (id_ruta) REFERENCES rutas(id_ruta) ON DELETE CASCADE
);

COMMENT ON TABLE tramos IS 'Tramos individuales que componen una ruta de transporte';
COMMENT ON COLUMN tramos.id_tramo IS 'Identificador único del tramo';
COMMENT ON COLUMN tramos.id_ruta IS 'Ruta a la que pertenece el tramo';
COMMENT ON COLUMN tramos.origen IS 'Punto de origen del tramo';
COMMENT ON COLUMN tramos.destino IS 'Punto de destino del tramo';
COMMENT ON COLUMN tramos.dominio_camion IS 'Dominio del camión asignado al tramo';
COMMENT ON COLUMN tramos.estado IS 'Estado del tramo: ESTIMADO, PENDIENTE, ASIGNADO, INICIADO, FINALIZADO';
COMMENT ON COLUMN tramos.fecha_hora_inicio_real IS 'Fecha y hora real de inicio del tramo';
COMMENT ON COLUMN tramos.fecha_hora_fin_real IS 'Fecha y hora real de finalización del tramo';
COMMENT ON COLUMN tramos.odometro_inicial IS 'Lectura del odómetro al inicio';
COMMENT ON COLUMN tramos.odometro_final IS 'Lectura del odómetro al finalizar';
COMMENT ON COLUMN tramos.costo_real IS 'Costo real del tramo (calculado al finalizar)';
COMMENT ON COLUMN tramos.tiempo_real IS 'Tiempo real del tramo en horas';
COMMENT ON COLUMN tramos.costo_aproximado IS 'Costo aproximado estimado del tramo';
COMMENT ON COLUMN tramos.fecha_hora_inicio_estimada IS 'Fecha y hora estimada de inicio';
COMMENT ON COLUMN tramos.fecha_hora_fin_estimada IS 'Fecha y hora estimada de finalización';
COMMENT ON COLUMN tramos.distancia_estimada_km IS 'Distancia estimada en kilómetros';
COMMENT ON COLUMN tramos.distancia_real_km IS 'Distancia real recorrida en kilómetros';

-- Índices para tramos
CREATE INDEX IF NOT EXISTS idx_tramos_ruta ON tramos(id_ruta);
CREATE INDEX IF NOT EXISTS idx_tramos_estado ON tramos(estado);
CREATE INDEX IF NOT EXISTS idx_tramos_camion ON tramos(dominio_camion);
CREATE INDEX IF NOT EXISTS idx_tramos_fecha_inicio ON tramos(fecha_hora_inicio_real);

-- =====================================================
-- Datos iniciales (opcional)
-- =====================================================

-- Insertar un cliente de ejemplo para testing
INSERT INTO clientes (nombre, apellido, email, telefono, direccion)
VALUES ('Cliente', 'Demo', 'demo@example.com', '1234567890', 'Calle Demo 123')
ON CONFLICT DO NOTHING;

-- =====================================================
-- Fin de V1__init.sql
-- =====================================================
-- Notas:
-- - Este script crea todas las tablas base del sistema
-- - Las migraciones V2 a V5 agregarán columnas adicionales
-- - Compatible con PostgreSQL 12+
-- - Incluye índices para optimizar consultas frecuentes
-- - Usa ON DELETE CASCADE para mantener integridad referencial
-- =====================================================

