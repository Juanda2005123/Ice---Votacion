@echo off
setlocal enabledelayedexpansion

echo ==========================================
echo SCRIPT DE DESPLIEGUE - SISTEMA DE VOTACION
echo ==========================================

REM 1. Limpiar y construir todo el proyecto
echo.
echo 1. Ejecutando gradle clean build...
call gradlew.bat clean build

if !errorlevel! neq 0 (
    echo ERROR: Fallo en gradle clean build
    pause
    exit /b 1
)

echo [OK] Gradle clean build completado exitosamente

REM 2. Eliminar JAR anteriores
echo.
echo 2. Eliminando JAR anteriores...

del /q "jar\broker mesa-votacion a lugar-votacion\*.jar" 2>nul
del /q "jar\broker lugar-votacion a departamento\*.jar" 2>nul
del /q "jar\mesa-votacion1\*.jar" 2>nul
del /q "jar\mesa-votacion2\*.jar" 2>nul
del /q "jar\lugar-votacion1\*.jar" 2>nul
del /q "jar\departamento1\*.jar" 2>nul
del /q "jar\servidor-central1\*.jar" 2>nul
del /q "jar\proxy1\*.jar" 2>nul

echo [OK] JAR anteriores eliminados

REM 3. Copiar nuevos JAR fat a las carpetas correspondientes
echo.
echo 3. Copiando nuevos JAR fat...

REM Broker para mesa-votacion a lugar-votacion
echo Copiando broker (mesa ^> lugar)...
copy "broker\build\libs\broker-fat.jar" "jar\broker mesa-votacion a lugar-votacion\" >nul 2>&1
if !errorlevel! equ 0 (
    echo [OK] broker-fat.jar copiado a 'broker mesa-votacion a lugar-votacion'
) else (
    echo [ERROR] Fallo al copiar broker-fat.jar (mesa ^> lugar)
)

REM Broker para lugar-votacion a departamento
echo Copiando broker (lugar ^> departamento)...
copy "broker\build\libs\broker-fat.jar" "jar\broker lugar-votacion a departamento\" >nul 2>&1
if !errorlevel! equ 0 (
    echo [OK] broker-fat.jar copiado a 'broker lugar-votacion a departamento'
) else (
    echo [ERROR] Fallo al copiar broker-fat.jar (lugar ^> departamento)
)

REM Mesa de votacion 1
echo Copiando mesa-votacion1...
copy "mesa-votacion\build\libs\mesa-votacion-fat.jar" "jar\mesa-votacion1\" >nul 2>&1
if !errorlevel! equ 0 (
    echo [OK] mesa-votacion-fat.jar copiado a 'mesa-votacion1'
) else (
    echo [ERROR] Fallo al copiar mesa-votacion-fat.jar a mesa-votacion1
)

REM Mesa de votacion 2
echo Copiando mesa-votacion2...
copy "mesa-votacion\build\libs\mesa-votacion-fat.jar" "jar\mesa-votacion2\" >nul 2>&1
if !errorlevel! equ 0 (
    echo [OK] mesa-votacion-fat.jar copiado a 'mesa-votacion2'
) else (
    echo [ERROR] Fallo al copiar mesa-votacion-fat.jar a mesa-votacion2
)

REM Lugar de votacion
echo Copiando lugar-votacion...
copy "lugar-votacion\build\libs\lugar-votacion-fat.jar" "jar\lugar-votacion1\" >nul 2>&1
if !errorlevel! equ 0 (
    echo [OK] lugar-votacion-fat.jar copiado a 'lugar-votacion1'
) else (
    echo [ERROR] Fallo al copiar lugar-votacion-fat.jar
)

REM Departamento
echo Copiando departamento...
copy "departamento\build\libs\departamento-fat.jar" "jar\departamento1\" >nul 2>&1
if !errorlevel! equ 0 (
    echo [OK] departamento-fat.jar copiado a 'departamento1'
) else (
    echo [ERROR] Fallo al copiar departamento-fat.jar
)

REM Servidor Central
echo Copiando servidor-central...
copy "servidor-central\build\libs\servidor-central-fat.jar" "jar\servidor-central1\" >nul 2>&1
if !errorlevel! equ 0 (
    echo [OK] servidor-central-fat.jar copiado a 'servidor-central1'
) else (
    echo [ERROR] Fallo al copiar servidor-central-fat.jar
)

REM Proxy
echo Copiando proxy...
copy "proxy\build\libs\proxy-fat.jar" "jar\proxy1\" >nul 2>&1
if !errorlevel! equ 0 (
    echo [OK] proxy-fat.jar copiado a 'proxy1'
) else (
    echo [ERROR] Fallo al copiar proxy-fat.jar
)

echo.
echo ==========================================
echo DESPLIEGUE COMPLETADO
echo ==========================================
echo.
echo JAR disponibles en:
echo ├── jar\broker mesa-votacion a lugar-votacion\broker-fat.jar
echo ├── jar\broker lugar-votacion a departamento\broker-fat.jar
echo ├── jar\mesa-votacion1\mesa-votacion-fat.jar
echo ├── jar\mesa-votacion2\mesa-votacion-fat.jar
echo ├── jar\lugar-votacion1\lugar-votacion-fat.jar
echo ├── jar\departamento1\departamento-fat.jar
echo ├── jar\servidor-central1\servidor-central-fat.jar
echo └── jar\proxy1\proxy-fat.jar
echo.
echo Listo para ejecutar!
echo.
pause
