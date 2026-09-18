-- Crea la base que usa logistica-service.
-- Las TABLAS no van aca: las genera Hibernate al arrancar (hbm2ddl.auto=update).
--
-- Se corre contra la base "postgres", no contra "logistica":
--   psql -U postgres -h localhost -d postgres -f crear-base.sql

CREATE DATABASE logistica;
