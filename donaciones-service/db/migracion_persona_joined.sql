-- Ejecutar en una base que todavía tiene la columna persona.tipo, con las
-- tablas persona_humana y persona_juridica ya creadas por Hibernate.
START TRANSACTION;

INSERT INTO persona_humana (persona_id, apellido, direccion, edad, genero)
SELECT p.persona_id, p.apellido, p.direccion, p.edad, p.genero
FROM persona p
LEFT JOIN persona_humana h ON h.persona_id = p.persona_id
WHERE p.tipo = 'HUMANA' AND h.persona_id IS NULL;

INSERT INTO persona_juridica (persona_id, razon_social, rubro)
SELECT p.persona_id, p.razon_social, p.rubro
FROM persona p
LEFT JOIN persona_juridica j ON j.persona_id = p.persona_id
WHERE p.tipo = 'JURIDICA' AND j.persona_id IS NULL;

COMMIT;

-- JOINED identifica el subtipo por su tabla. Las filas nuevas no envían tipo.
ALTER TABLE persona MODIFY COLUMN tipo VARCHAR(31) NULL;
