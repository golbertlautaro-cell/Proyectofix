-- V4__add_tramo_orden.sql
-- Agrega la columna orden (obligatoria) a la tabla tramos

-- Agregar columna orden con valor temporal
ALTER TABLE tramos
  ADD COLUMN IF NOT EXISTS orden integer;

-- Asignar valores de orden a registros existentes usando CTE
WITH orden_calculado AS (
  SELECT id_tramo, ROW_NUMBER() OVER (PARTITION BY id_ruta ORDER BY id_tramo) AS nuevo_orden
  FROM tramos
  WHERE orden IS NULL
)
UPDATE tramos
SET orden = orden_calculado.nuevo_orden
FROM orden_calculado
WHERE tramos.id_tramo = orden_calculado.id_tramo;

-- Hacer la columna NOT NULL después de asignar valores
ALTER TABLE tramos
  ALTER COLUMN orden SET NOT NULL;

-- Comentario
COMMENT ON COLUMN tramos.orden IS 'Orden del tramo dentro de la ruta (1, 2, 3...)';

