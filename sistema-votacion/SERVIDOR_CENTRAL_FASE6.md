# Servidor Central - Fase 6: Reduce Final

## Resumen Ejecutivo

La **Fase 6** implementa el **destino final** del sistema Map-Reduce en el módulo `servidor-central`, transformándolo de un simple receptor de votos individuales a un **consolidador nacional** que procesa deltas departamentales usando thread pools y genera el conteo nacional final con reporte CSV.

## Arquitectura Reduce Final

### Flujo de Datos
```
[Depto-1] ---> [Delta-Dept-1] ---┐
[Depto-2] ---> [Delta-Dept-2] ---┤---> [SERVIDOR CENTRAL] ---> [resumen.csv]
[Depto-N] ---> [Delta-Dept-N] ---┘      (Reduce Final)        (Conteo Nacional)
```

### Componentes Principales

#### 1. **Thread Pool Concurrente**
- **Archivo**: `ServidorController.java`
- **Función**: Procesa deltas departamentales en paralelo
- **Implementación**: `ExecutorService` con pool configurable
- **Configuración**: `threads.pool.size=8` en properties

#### 2. **Consolidación Nacional**
- **Archivo**: `consolidacion/ConsolidadorNacional.java`
- **Función**: Conteo nacional thread-safe final
- **Estructura**: `ConcurrentHashMap<Integer, AtomicInteger>`
- **Operaciones**: Consolidación atómica de deltas departamentales

#### 3. **Generación de Reportes**
- **Archivo**: `consolidacion/GeneradorCSV.java`
- **Función**: Genera archivo `resumen.csv` con resultados finales
- **Formato**: `candidateId,totalVotes`
- **Trigger**: Al cerrar el servidor central

#### 4. **Interfaz de Usuario**
- **Archivo**: `ui/ServidorUI.java`
- **Función**: Menú interactivo con estadísticas en tiempo real
- **Opciones**: Ver estadísticas y salir con generación de CSV

## Cambios Implementados

### Archivos Creados

1. **`consolidacion/ConsolidadorNacional.java`**
   - Consolidación thread-safe del conteo nacional
   - Uso de `ConcurrentHashMap` y `AtomicInteger`
   - Método `consolidarDelta()` para Reduce Final

2. **`consolidacion/GeneradorCSV.java`**
   - Generación de reportes CSV finales
   - Formato estándar: `candidateId,totalVotes`
   - Manejo de archivos con sobreescritura

3. **`ui/ServidorUI.java`**
   - Interfaz de usuario interactiva
   - Estadísticas nacionales en tiempo real
   - Menú principal con opción de salir y generar CSV

### Archivos Refactorizados

#### 1. **`ServidorController.java`**
**Cambios Principales:**
- ❌ **ELIMINADO**: Toda lógica de votos individuales
- ✅ **AGREGADO**: Thread pool para procesamiento concurrente
- ✅ **AGREGADO**: Integración con consolidador nacional
- ✅ **AGREGADO**: Generación automática de CSV al cerrar

**Métodos Clave:**
```java
// REDUCE FINAL - Procesamiento concurrente
public boolean procesarDelta(DeltaConteo delta)

// ESTADÍSTICAS - Acceso al conteo nacional
public Map<Integer, Integer> obtenerConteoNacional()

// CIERRE - Generación de reporte final
public void cerrarYGenerarReporte()
```

#### 2. **`ServidorIceServidor.java`**
**Cambios Principales:**
- ❌ **ELIMINADO**: `recibirVoto()` y conversión de votos
- ✅ **SIMPLIFICADO**: Solo `recibirDeltaConteo()`
- ✅ **DELEGACIÓN**: Directa al controller Reduce Final

#### 3. **`ConfiguracionServidor.java`**
**Cambios Principales:**
- ✅ **AGREGADO**: `getThreadPoolSize()` para configuración de concurrencia

#### 4. **`ServidorCentralApp.java`**
**Cambios Principales:**
- ✅ **AGREGADO**: Integración con UI interactiva
- ✅ **MEJORADO**: Shutdown hook con generación de CSV

### Configuración Externalizada

#### `servidor-central.properties`
```properties
# Thread Pool para Reduce Final
threads.pool.size=8            # Hilos para procesamiento concurrente

# Servidor Central Identity
nodo.id=SERVIDOR-CENTRAL-001
nodo.nombre=Servidor Central de Votacion

# Connectivity
servidor.host=localhost
servidor.puerto=6000
servidor.timeout=5000
```

## Funcionalidades Eliminadas

### ❌ Código Legacy Removido
1. **Procesamiento de votos individuales**
2. **Conversión voto → conteo** (ahora recibe deltas directamente)
3. **Validación de ciudadanos** (no corresponde al servidor central)
4. **Logging verboso** (sistema optimizado para performance)

## Funcionalidades Nuevas

### ✅ Nuevas Capacidades
1. **Consolidación Nacional**: Conteo final thread-safe
2. **Procesamiento Concurrente**: Thread pool para deltas departamentales
3. **Interfaz Interactiva**: UI con estadísticas en tiempo real
4. **Reporte Automático**: Generación de `resumen.csv` al cerrar
5. **Estadísticas Live**: Visualización del conteo nacional actualizado

## Interfaz de Usuario

### Menú Principal
```
==================================================
    SERVIDOR CENTRAL DE VOTACIÓN
    Conteo Nacional en Tiempo Real
==================================================

------------------------------
MENÚ PRINCIPAL
------------------------------

ESTADISTICAS NACIONALES:
Total de votos: 150,000
Candidatos: 5

Conteo por candidato:
  Candidato 1: 45,000 votos
  Candidato 2: 38,000 votos
  Candidato 3: 32,000 votos
  Candidato 4: 25,000 votos
  Candidato 5: 10,000 votos

Opciones:
1. Actualizar estadisticas
2. Salir y generar reporte CSV

Seleccione una opción: 
```

### Generación de CSV
Al seleccionar "Salir", el sistema:
1. Muestra estadísticas finales
2. Genera `resumen.csv` automáticamente
3. Cierra ordenadamente todos los recursos

### Formato del Archivo `resumen.csv`
```csv
candidateId,totalVotes
1,45000
2,38000
3,32000
4,25000
5,10000
```

## Verificación de Implementación

### Compilación Exitosa
```bash
.\gradlew :servidor-central:clean :servidor-central:build
# BUILD SUCCESSFUL in 3s
# 12 actionable tasks: 10 executed, 2 up-to-date
```

### Estructura de Clases
```
servidor-central/
├── controller/
│   └── ServidorController.java          # Reduce Final orchestrator
├── consolidacion/
│   ├── ConsolidadorNacional.java        # Thread-safe national consolidation
│   └── GeneradorCSV.java                # CSV report generation
├── comunicacion/
│   └── ServidorIceServidor.java         # Ice server (deltas only)
├── config/
│   └── ConfiguracionServidor.java       # Externalized config
├── ui/
│   └── ServidorUI.java                  # Interactive user interface
└── ServidorCentralApp.java              # Main application
```

## Beneficios de la Refactorización

### 🚀 Performance
- **Concurrencia**: Thread pool para deltas departamentales
- **Atomicidad**: Operaciones thread-safe sin bloqueos
- **Eficiencia**: Consolidación final optimizada

### 🔒 Thread Safety
- **Lock-Free**: `ConcurrentHashMap` + `AtomicInteger`
- **Escalabilidad**: Thread pool dimensionable
- **Consistencia**: Conteo nacional garantizado

### 📊 Monitoreo
- **Tiempo Real**: Estadísticas actualizadas automáticamente
- **Interactividad**: UI responsive para administradores
- **Reportes**: CSV automático con formato estándar

### 🏗️ Arquitectura
- **Separación clara**: UI, lógica de negocio, persistencia
- **Configurabilidad**: Parámetros externalizados
- **Robustez**: Manejo ordenado de recursos y cierre

## Instrucciones de Uso

### Ejecución
```bash
cd servidor-central-jar-directory
java -jar servidor-central.jar
```

### Operación
1. **Inicio**: El servidor muestra la UI interactiva
2. **Monitoreo**: Opción 1 actualiza estadísticas en tiempo real
3. **Cierre**: Opción 2 genera `resumen.csv` y cierra el servidor

### Archivos Generados
- **`resumen.csv`**: Conteo nacional final por candidato
- **`servidor-central.log`**: Logs del sistema (si configurado)

---

**Autor**: Sistema de Votación  
**Versión**: 3.0 - Reduce Final  
**Fecha**: Junio 2025  
**Status**: ✅ IMPLEMENTADO Y COMPILADO
