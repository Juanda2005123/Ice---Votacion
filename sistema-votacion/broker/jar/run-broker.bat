@echo off
echo ========================================
echo EJECUTANDO BROKER DE VOTACIÓN
echo ========================================

:: Verificar que el JAR existe
if not exist "broker.jar" (
    echo ERROR: No se encuentra broker.jar en el directorio actual
    echo Ejecute deploy-broker.bat primero
    exit /b 1
)

:: Verificar que la configuración existe
if not exist "broker.properties" (
    echo ERROR: No se encuentra broker.properties en el directorio actual
    echo Asegurese de tener el archivo de configuración
    exit /b 1
)

echo Iniciando broker...
echo Configuración: broker.properties
echo.

:: Ejecutar el broker
java -jar broker.jar

:: Capturar código de salida
if errorlevel 1 (
    echo.
    echo ERROR: El broker terminó con errores
    exit /b 1
)

echo.
echo Broker finalizado correctamente
