-- Para bases existentes: revisar primero si hay nombres repetidos (sin importar mayúsculas).
SELECT LOWER(TRIM(nombre_usuario)), COUNT(*)
FROM usuario
GROUP BY LOWER(TRIM(nombre_usuario))
HAVING COUNT(*) > 1;

-- Si la consulta devuelve filas, resolver esas cuentas antes de agregar la restricción.
-- No ejecutar si Hibernate ya creó uk_usuario_nombre al iniciar con hbm2ddl.auto=update.
ALTER TABLE usuario ADD CONSTRAINT uk_usuario_nombre UNIQUE (nombre_usuario);
