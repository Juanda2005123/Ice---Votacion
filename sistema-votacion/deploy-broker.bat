@echo off
echo ========================================
echo COMPILANDO Y DESPLEGANDO BROKER
echo ========================================

:: Compilar el broker
echo Compilando broker...
call gradlew :broker:build -q
if errorlevel 1 (
    echo ERROR: Falló la compilación del broker
    exit /b 1
)

:: Crear directorio de deployment
if not exist "broker\jar" mkdir "broker\jar"

:: Copiar JAR compilado
copy "broker\build\libs\broker.jar" "broker\jar\" >nul
if errorlevel 1 (
    echo ERROR: No se pudo copiar el JAR del broker
    exit /b 1
)

:: Copiar archivo de configuración
copy "broker\src\main\resources\broker.properties" "broker\jar\" >nul
if errorlevel 1 (
    echo ERROR: No se pudo copiar broker.properties
    exit /b 1
)

echo.
echo ========================================
echo BROKER DESPLEGADO EXITOSAMENTE
echo ========================================
echo JAR: broker\jar\broker.jar
echo Config: broker\jar\broker.properties
echo.
echo Para ejecutar:
echo   cd broker\jar
echo   java -jar broker.jar
echo ========================================
