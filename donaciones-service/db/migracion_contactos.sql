-- Ejecutar una vez sobre la base donaciones antes de desplegar esta versión.
-- Conserva los contactos existentes y habilita varios contactos por notificable.
ALTER TABLE contacto
    ADD COLUMN contacto_id BIGINT NOT NULL AUTO_INCREMENT UNIQUE FIRST,
    ADD COLUMN medio_predeterminado BOOLEAN NOT NULL DEFAULT FALSE,
    MODIFY COLUMN tipo_notificable VARCHAR(16) NOT NULL;

ALTER TABLE contacto
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (contacto_id);
