-- V5__add_solicitud_costos_tiempos_totales.sql
-- Agrega campos de costos y tiempos totales a la tabla solicitudes

-- Agregar columnas de costos y tiempos totales
ALTER TABLE solicitudes
  ADD COLUMN IF NOT EXISTS costo_total_estimado double precision,
  ADD COLUMN IF NOT EXISTS costo_total_real double precision,
  ADD COLUMN IF NOT EXISTS tiempo_total_estimado_horas double precision,
  ADD COLUMN IF NOT EXISTS tiempo_total_real_horas double precision;

-- Comentarios para documentación
COMMENT ON COLUMN solicitudes.costo_total_estimado IS 'Costo total estimado de todas las rutas y tramos';
COMMENT ON COLUMN solicitudes.costo_total_real IS 'Costo total real de todas las rutas y tramos';
COMMENT ON COLUMN solicitudes.tiempo_total_estimado_horas IS 'Tiempo total estimado en horas';
COMMENT ON COLUMN solicitudes.tiempo_total_real_horas IS 'Tiempo total real en horas';

