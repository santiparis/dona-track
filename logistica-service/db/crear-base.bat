@echo off
REM Crea la base de logistica-service. Pide la password del usuario postgres.
REM Las tablas las crea Hibernate al levantar logistica.Main.

set PSQL="C:\Program Files\PostgreSQL\17\bin\psql.exe"

if not exist %PSQL% (
  echo No encontre psql en %PSQL%
  echo Editá esta linea con la ruta de tu instalacion de PostgreSQL.
  exit /b 1
)

%PSQL% -U postgres -h localhost -p 5432 -d postgres -f "%~dp0crear-base.sql"

if errorlevel 1 (
  echo.
  echo Fallo. Si dice "already exists", la base ya estaba creada y esta todo bien.
  exit /b 1
)

echo.
echo Base "logistica" creada. Ahora levanta logistica.Main y Hibernate arma las tablas.
