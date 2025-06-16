# Sistema de Votación - Map-Reduce Distribuido Completo

## Resumen Ejecutivo

El **Sistema de Votación** ha sido completamente refactorizado para implementar una arquitectura **Map-Reduce distribuida de 3 niveles**, optimizada para procesamiento masivo y concurrente de votos con consolidación automática y reportes finales en tiempo real.

## Arquitectura General Map-Reduce

### Flujo de Datos Completo
```
[MESA] → [Votos] → [LUGAR] →    [Delta-L] → [DEPTO] →   [Delta-D] → [SERVIDOR] → [CSV]
                   Map-Reduce-1            Map-Reduce-2              Reduce-Final
                   (Consolidación         (Consolidación             (Conteo Nacional
                    por Lugar)             Departamental)            + Reporte CSV)
```

### Niveles de Consolidación

#### **Nivel 1: Mesa → Lugar (Map-Reduce Level 1)**
- **Entrada**: Votos individuales desde mesas de votación
- **Proceso**: MAP (Thread Pool) + REDUCE (Deltas por lugar)
- **Salida**: Deltas consolidados por lugar de votación
- **Umbral**: Configurable (votos/tiempo)

#### **Nivel 2: Lugar → Departamento (Map-Reduce Level 2)**
- **Entrada**: Deltas consolidados de lugares
- **Proceso**: MAP (Thread Pool) + REDUCE (Deltas departamentales)
- **Salida**: Deltas consolidados por departamento
- **Umbral**: Configurable (votos/tiempo)

#### **Nivel 3: Departamento → Servidor Central (Reduce Final)**
- **Entrada**: Deltas consolidados departamentales
- **Proceso**: REDUCE FINAL (Thread Pool + Conteo Nacional)
- **Salida**: Conteo nacional final + reporte CSV
- **UI**: Interfaz interactiva con estadísticas en tiempo real

## Módulos Refactorizados

### 1. **Mesa de Votación** (Sin cambios)
- **Status**: ✅ MANTENIDO
- **Función**: Generación de votos individuales
- **Salida**: Archivos CSV con votos

### 2. **Lugar de Votación** (Map-Reduce Level 1)
- **Status**: ✅ REFACTORIZADO - Fase 4
- **Función**: Consolida votos → deltas por lugar
- **Tecnologías**: ExecutorService, ConcurrentHashMap, ScheduledExecutorService
- **Configuración**: `threads.pool.size`, `delta.umbral.votos`, `delta.umbral.tiempo`

### 3. **Departamento** (Map-Reduce Level 2)  
- **Status**: ✅ REFACTORIZADO - Fase 5
- **Función**: Consolida deltas de lugares → deltas departamentales
- **Tecnologías**: ExecutorService, ConcurrentHashMap, Callback Pattern
- **Configuración**: `threads.pool.size`, `delta.umbral.votos`, `delta.umbral.tiempo`

### 4. **Servidor Central** (Reduce Final)
- **Status**: ✅ REFACTORIZADO - Fase 6
- **Función**: Consolida deltas departamentales → conteo nacional + CSV
- **Tecnologías**: ExecutorService, ConcurrentHashMap, UI interactiva
- **Configuración**: `threads.pool.size`
- **Reporte**: `resumen.csv` automático

## Configuraciones por Módulo

### Lugar de Votación (`lugar-votacion.properties`)
```properties
# Map-Reduce Level 1
delta.umbral.votos=1000        # Umbral de votos para REDUCE
delta.umbral.tiempo=2000       # Umbral de tiempo para REDUCE (ms)
threads.pool.size=4            # Hilos para MAP phase

# Conectividad
departamento.host=localhost
departamento.puerto=7001
```

### Departamento (`departamento.properties`)
```properties
# Map-Reduce Level 2  
delta.umbral.votos=2000        # Umbral de votos para REDUCE
delta.umbral.tiempo=3000       # Umbral de tiempo para REDUCE (ms)
threads.pool.size=8            # Hilos para MAP phase

# Conectividad
servidor.central.host=localhost
servidor.central.puerto=6000
```

### Servidor Central (`servidor-central.properties`)
```properties
# Reduce Final
threads.pool.size=8            # Hilos para procesamiento concurrente

# Server
servidor.host=localhost
servidor.puerto=6000
```

## Tecnologías Implementadas

### Concurrencia y Thread Safety
- **ExecutorService**: Thread pools configurables para MAP phases
- **ConcurrentHashMap**: Estado compartido thread-safe
- **AtomicInteger**: Operaciones atómicas sin locks
- **ScheduledExecutorService**: REDUCE phases temporizados

### Patrones de Diseño
- **Map-Reduce**: Procesamiento distribuido escalable
- **Callback Pattern**: Desacoplamiento entre MAP y REDUCE
- **Template Method**: Estructura común en todos los módulos
- **Strategy Pattern**: Configuración externalizada

### Comunicación
- **ZeroC Ice**: Middleware de comunicación distribuida
- **Interfaces IDL**: Definición de contratos de servicio
- **Proxies**: Cliente-servidor transparente

## Instrucciones de Compilación

### Compilación Individual
```bash
# Lugar de votación
.\gradlew :lugar-votacion:clean :lugar-votacion:build

# Departamento  
.\gradlew :departamento:clean :departamento:build

# Servidor Central
.\gradlew :servidor-central:clean :servidor-central:build
```

### Compilación Completa
```bash
# Todo el sistema
.\gradlew clean build
```

## Instrucciones de Ejecución

### Orden de Inicio (Recomendado)
```bash
# 1. Servidor Central (Puerto 6000)
cd servidor-central-jar
java -jar servidor-central.jar

# 2. Departamento (Puerto 7001)  
cd departamento-jar
java -jar departamento.jar

# 3. Lugar de Votación (Puerto 8001)
cd lugar-votacion-jar
java -jar lugar-votacion.jar

# 4. Mesa de Votación (Genera votos)
cd mesa-votacion-jar
java -jar mesa-votacion.jar
```

### Configuración de Red
- **Servidor Central**: `localhost:6000`
- **Departamento**: `localhost:7001` 
- **Lugar de Votación**: `localhost:8001`
- **Comunicación**: ZeroC Ice TCP

## Pruebas del Sistema Completo

### Escenario de Prueba Básico

#### Paso 1: Iniciar Servidor Central
```bash
cd servidor-central-jar
java -jar servidor-central.jar
```
**Resultado Esperado**: UI interactiva mostrando estadísticas en tiempo real

#### Paso 2: Iniciar Departamento
```bash
cd departamento-jar  
java -jar departamento.jar
```
**Resultado Esperado**: Conexión exitosa con servidor central

#### Paso 3: Iniciar Lugar de Votación
```bash
cd lugar-votacion-jar
java -jar lugar-votacion.jar
```
**Resultado Esperado**: Conexión exitosa con departamento

#### Paso 4: Generar Votos
```bash
cd mesa-votacion-jar
java -jar mesa-votacion.jar
```
**Resultado Esperado**: Generación de archivo CSV con votos

#### Paso 5: Procesar Votos en Lugar
- Los votos son procesados automáticamente
- Se consolidan en deltas por lugar
- Se envían al departamento cuando se alcanza el umbral

#### Paso 6: Monitorear en Servidor Central
- La UI muestra estadísticas actualizadas en tiempo real
- El conteo nacional se actualiza automáticamente
- Opción 1: Actualizar estadísticas manualmente
- Opción 2: Salir y generar reporte CSV

#### Paso 7: Generar Reporte Final
- Seleccionar opción 2 en la UI del servidor central
- Se genera automáticamente `resumen.csv`
- Formato: `candidateId,totalVotes`

### Verificación de Resultados

#### Archivos Generados
1. **Mesa**: `votos_mesa_XXX.csv` (votos individuales)
2. **Servidor**: `resumen.csv` (conteo nacional final)

#### Consistencia de Datos
- **Total votos generados** = **Total votos en resumen.csv**
- **Distribución por candidato** debe ser coherente
- **Sin pérdida de datos** en toda la cadena Map-Reduce

### Pruebas de Carga

#### Configuración Recomendada para Carga
```properties
# Lugar
threads.pool.size=16
delta.umbral.votos=500
delta.umbral.tiempo=1000

# Departamento
threads.pool.size=16  
delta.umbral.votos=1000
delta.umbral.tiempo=1500

# Servidor Central
threads.pool.size=16
```

#### Escenarios de Carga
- **Baja**: 1,000 votos, 5 candidatos
- **Media**: 10,000 votos, 10 candidatos  
- **Alta**: 100,000 votos, 20 candidatos
- **Extrema**: 1,000,000 votos, 50 candidatos

## Beneficios del Sistema Refactorizado

### 🚀 Performance
- **Paralelización**: Procesamiento concurrente en todos los niveles
- **Consolidación**: Reducción masiva de tráfico de red
- **Escalabilidad**: Thread pools dimensionables según carga

### 🔒 Confiabilidad  
- **Thread Safety**: Operaciones atómicas garantizadas
- **Tolerancia a Fallas**: Configuración de reintentos
- **Consistencia**: Estado compartido thread-safe

### 📊 Monitoreo
- **Tiempo Real**: Estadísticas live en servidor central
- **Reportes**: CSV automático con formato estándar
- **Trazabilidad**: Logs estructurados en cada nivel

### 🏗️ Arquitectura
- **Modularidad**: Separación clara de responsabilidades
- **Configurabilidad**: Parámetros externalizados
- **Extensibilidad**: Fácil adición de nuevos niveles

## Troubleshooting

### Problemas Comunes

#### Error de Conectividad
```
[ERROR] No se pudo conectar con servidor central
```
**Solución**: Verificar que el servidor central esté iniciado en puerto 6000

#### Umbral No Alcanzado
```
No se envían deltas aunque hay votos procesados
```
**Solución**: Reducir `delta.umbral.votos` o `delta.umbral.tiempo` en configuración

#### Thread Pool Saturado
```
Procesamiento lento de deltas
```
**Solución**: Aumentar `threads.pool.size` en configuración

#### CSV No Generado
```
Archivo resumen.csv no se crea al cerrar
```
**Solución**: Usar opción 2 en UI del servidor central, no cerrar forzadamente

## Próximos Pasos

### Optimizaciones Futuras
1. **Balanceador de Carga**: Múltiples instancias por nivel
2. **Persistencia**: Base de datos para estado intermedio
3. **Monitoreo Avanzado**: Métricas de performance en tiempo real
4. **API REST**: Exposición de estadísticas vía HTTP

### Extensiones Posibles  
1. **Nivel Regional**: Map-Reduce Level 3 entre departamento y servidor
2. **Cache Distribuido**: Redis para estado compartido
3. **Message Queue**: Apache Kafka para comunicación asíncrona
4. **Dashboard Web**: UI web para monitoreo remoto

---

**Autor**: Sistema de Votación  
**Versión**: 3.0 - Map-Reduce Distribuido Completo  
**Fecha**: Junio 2025  
**Status**: ✅ SISTEMA COMPLETO IMPLEMENTADO Y COMPILADO

**Módulos Completados**:
- ✅ Mesa de Votación (Original)
- ✅ Lugar de Votación (Fase 4 - Map-Reduce L1)  
- ✅ Departamento (Fase 5 - Map-Reduce L2)
- ✅ Servidor Central (Fase 6 - Reduce Final)

**Compilación Exitosa**: Todos los módulos compilan sin errores  
**Funcionalidad Completa**: Map-Reduce de 3 niveles operativo  
**UI Interactiva**: Servidor central con estadísticas en tiempo real  
**Reportes**: Generación automática de CSV con conteo nacional
