-- V2__add_contenedores_columns.sql
-- Agrega las columnas requeridas por la entidad Contenedor
ALTER TABLE contenedores
  ADD COLUMN IF NOT EXISTS peso_real double precision,
  ADD COLUMN IF NOT EXISTS volumen_real double precision,
  ADD COLUMN IF NOT EXISTS deposito_actual_id bigint;

