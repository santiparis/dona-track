-- Ejecutar sobre una base existente que todavía usa camiones.disponibilidad.
BEGIN;

ALTER TABLE camiones ADD COLUMN IF NOT EXISTS estado VARCHAR(255);

UPDATE camiones
SET estado = CASE
    WHEN disponibilidad IS TRUE THEN 'DISPONIBLE'
    WHEN disponibilidad IS FALSE THEN 'EN_RUTA'
    ELSE 'NO_DISPONIBLE'
END
WHERE estado IS NULL;

ALTER TABLE camiones ALTER COLUMN estado SET NOT NULL;

COMMIT;
