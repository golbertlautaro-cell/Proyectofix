-- V3__update_tramo_depositos.sql
-- Actualiza la tabla tramos: elimina deposito_id y agrega campos para origen/destino con depósitos o direcciones libres

-- Eliminar columna antigua si existe
ALTER TABLE tramos DROP COLUMN IF EXISTS deposito_id;

-- Agregar nuevas columnas para depósitos de origen y destino
ALTER TABLE tramos
  ADD COLUMN IF NOT EXISTS origen_deposito_id bigint,
  ADD COLUMN IF NOT EXISTS destino_deposito_id bigint,
  ADD COLUMN IF NOT EXISTS origen_direccion_libre varchar(200),
  ADD COLUMN IF NOT EXISTS destino_direccion_libre varchar(200),
  ADD COLUMN IF NOT EXISTS tiempo_estadia_horas double precision,
  ADD COLUMN IF NOT EXISTS costo_estadia_real double precision;

-- Comentarios para documentación
COMMENT ON COLUMN tramos.origen_deposito_id IS 'ID del depósito de origen (opcional si se usa dirección libre)';
COMMENT ON COLUMN tramos.destino_deposito_id IS 'ID del depósito de destino (opcional si se usa dirección libre)';
COMMENT ON COLUMN tramos.origen_direccion_libre IS 'Dirección libre de origen (alternativa a deposito)';
COMMENT ON COLUMN tramos.destino_direccion_libre IS 'Dirección libre de destino (alternativa a deposito)';
COMMENT ON COLUMN tramos.tiempo_estadia_horas IS 'Tiempo de estadía en horas';
COMMENT ON COLUMN tramos.costo_estadia_real IS 'Costo real de estadía en depósito';

