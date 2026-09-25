@echo off
setlocal

set "ROOT_DIR=%~dp0"

echo Iniciando donaciones-service en http://localhost:8081
start "donaciones-service" cmd /k "cd /d ""%ROOT_DIR%"" && mvn -q -f donaciones-service/pom.xml -DskipTests compile exec:java -Dexec.mainClass=donaciones.Main -Dexec.cleanupDaemonThreads=false"

echo Iniciando logistica-service en http://localhost:7070
start "logistica-service" cmd /k "cd /d ""%ROOT_DIR%"" && mvn -q -f logistica-service/pom.xml -DskipTests compile exec:java -Dexec.mainClass=logistica.Main -Dexec.cleanupDaemonThreads=false"

echo.
echo Los servicios quedaron corriendo en ventanas separadas.
echo Donaciones: http://localhost:8081
echo Logistica : http://localhost:7070
echo.
echo Cierra ambas ventanas para detenerlos.

endlocal
