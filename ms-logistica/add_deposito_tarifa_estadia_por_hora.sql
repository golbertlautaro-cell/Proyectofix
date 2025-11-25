-- add_deposito_tarifa_estadia_por_hora.sql
-- Script para agregar campo tarifa_estadia_por_hora a la tabla depositos
-- Base de datos: logistica_db

-- Agregar columna tarifa_estadia_por_hora
ALTER TABLE depositos
  ADD COLUMN IF NOT EXISTS tarifa_estadia_por_hora double precision;

-- Comentario para documentación
COMMENT ON COLUMN depositos.tarifa_estadia_por_hora IS 'Tarifa de estadía por hora en el depósito';

-- Opcional: Calcular valor inicial basado en costo_estadia_diario (24 horas)
UPDATE depositos
SET tarifa_estadia_por_hora = costo_estadia_diario / 24.0
WHERE tarifa_estadia_por_hora IS NULL AND costo_estadia_diario IS NOT NULL;

