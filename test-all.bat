@echo off
echo.
echo ========================================
echo  donaciones-service
echo ========================================
mvn test -f donaciones-service/pom.xml
if %errorlevel% neq 0 (
    echo.
    echo FALLO: donaciones-service
    exit /b 1
)

echo.
echo ========================================
echo  logistica-service
echo ========================================
mvn test -f logistica-service/pom.xml
if %errorlevel% neq 0 (
    echo.
    echo FALLO: logistica-service
    exit /b 1
)

echo.
echo ========================================
echo  TODO OK
echo ========================================