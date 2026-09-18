-- Vacia las tablas para repetir las pruebas de Postman desde cero.
-- Las cuatro juntas porque hay FK entre ellas, y RESTART IDENTITY para que
-- los id vuelvan a empezar en 1.
--
-- Se corre contra "logistica":
--   psql -U postgres -h localhost -d logistica -f reset-datos.sql

TRUNCATE donaciones_encoladas, entregas, rutas, camiones RESTART IDENTITY CASCADE;
