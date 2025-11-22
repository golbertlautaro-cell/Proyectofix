-- init-db-seed.sql
-- Seed data for both microservices (20+ entries per microservice)
-- Execute this after Hibernate creates the tables
-- Tables are created automatically by Hibernate with ddl-auto: update

-- ====== LOGISTICA_DB SEEDS ======

INSERT INTO camiones (dominio, capacidad_peso, capacidad_volumen, consumo_promedio, costo_base_km, disponibilidad, nombre_transportista, telefono)
SELECT
  'DOM-' || LPAD(gs::text, 3, '0'),
  2000.0 + gs * 50,
  15.0 + gs,
  4.0 + ((gs % 5)::numeric * 0.1),
  8.0 + ((gs % 10)::numeric * 0.1),
  (gs % 2 = 0),
  'Transporte ' || gs,
  '1555' || LPAD(gs::text, 4, '0')
FROM generate_series(1,20) AS gs
ON CONFLICT (dominio) DO NOTHING;

INSERT INTO depositos (nombre, direccion, costo_estadia_diario, latitud, longitud)
SELECT 
  'Deposito ' || gs, 
  'Calle ' || gs || ', Buenos Aires',
  100.0 + gs,
  -34.60 + (gs * 0.01)::numeric,
  -58.38 - (gs * 0.01)::numeric
FROM generate_series(1,10) AS gs
ON CONFLICT DO NOTHING;

-- ====== SOLICITUDES_DB SEEDS ======

INSERT INTO clientes (nombre, apellido, direccion, telefono, email)
SELECT 
  'Cliente ' || gs, 
  'Apellido ' || gs,
  'Calle ' || gs || ', Buenos Aires',
  '1144' || LPAD(gs::text, 4, '0'),
  'cliente' || gs || '@example.com'
FROM generate_series(1,20) AS gs
ON CONFLICT DO NOTHING;

INSERT INTO solicitudes (id_cliente, fecha_creacion, estado)
SELECT 
  gs,
  now() + (gs || ' minutes')::interval,
  'PROGRAMADA'
FROM generate_series(1,20) AS gs
ON CONFLICT DO NOTHING;
