-- Ejecutar una sola vez sobre una base existente, antes de iniciar la nueva version.
-- Hasta este cambio, solo Persona (donante) tenia un Usuario asociado.
ALTER TABLE usuario
    ADD COLUMN rol VARCHAR(255) NOT NULL DEFAULT 'DONANTE';

-- Las nuevas cuentas deben indicar su rol explicitamente.
ALTER TABLE usuario ALTER COLUMN rol DROP DEFAULT;
