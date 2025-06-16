# Refactorización Departamento - Fase 5: Map-Reduce Level 2

## Resumen Ejecutivo

La **Fase 5** implementa el segundo nivel del patrón **Map-Reduce** en el módulo `departamento`, transformándolo de un simple retransmisor de votos individuales a un **consolidador departamental** que procesa deltas de múltiples lugares de votación y genera deltas departamentales consolidados hacia el servidor central.

## Arquitectura Map-Reduce Level 2

### Flujo de Datos
```
[Lugar 1] ---> [Delta-Place-1] ---┐
[Lugar 2] ---> [Delta-Place-2] ---┤---> [DEPARTAMENTO] ---> [Delta-Dept] ---> [SERVIDOR CENTRAL]
[Lugar N] ---> [Delta-Place-N] ---┘      (Map-Reduce L2)
```

### Componentes Principales

#### 1. **MAP PHASE - Thread Pool Departamental**
- **Archivo**: `DepartamentoController.java`
- **Función**: Procesa deltas de lugares en paralelo usando thread pool
- **Implementación**: `ExecutorService` con pool configurable
- **Thread Safety**: Garantizado por `ConcurrentHashMap`

#### 2. **SHARED STATE - Repositorio de Deltas**
- **Archivo**: `deltas/RepositorioDeltas.java`
- **Función**: Consolida conteos de múltiples lugares thread-safe
- **Estructura**: `ConcurrentHashMap<Integer, AtomicInteger>`
- **Atomicidad**: Operaciones atómicas con `AtomicInteger`

#### 3. **REDUCE PHASE - Generador de Deltas**
- **Archivo**: `deltas/GeneradorDeltas.java`
- **Función**: Genera y envía deltas departamentales consolidados
- **Triggers**: Por umbral de votos O tiempo transcurrido
- **Scheduler**: `ScheduledExecutorService` single-thread

## Cambios Implementados

### Archivos Creados
1. **`deltas/RepositorioDeltas.java`**
   - Consolidación thread-safe de deltas entrantes
   - Uso de `ConcurrentHashMap` y `AtomicInteger`
   - Método `consolidarDelta()` para MAP phase

2. **`deltas/GeneradorDeltas.java`**
   - REDUCE phase con scheduler automático
   - Callback pattern para envío de deltas
   - Manejo de umbrales configurables

### Archivos Refactorizados

#### 1. **`DepartamentoController.java`**
**Cambios Principales:**
- ❌ **ELIMINADO**: Toda lógica de votos individuales
- ✅ **AGREGADO**: Thread pool para MAP phase
- ✅ **AGREGADO**: Integración Map-Reduce departamental
- ✅ **AGREGADO**: Callback para envío de deltas consolidados

**Métodos Clave:**
```java
// MAP PHASE - Procesamiento paralelo
public boolean procesarDelta(DeltaConteo delta)

// CALLBACK - Envío al servidor central  
private void enviarDeltaConsolidado(DeltaConteo deltaConsolidado)
```

#### 2. **`ServidorIceDepartamento.java`**
**Cambios Principales:**
- ❌ **ELIMINADO**: `recibirVoto()` y conversión de votos
- ✅ **SIMPLIFICADO**: Solo `recibirDeltaConteo()`
- ✅ **DELEGACIÓN**: Directa al controller Map-Reduce

#### 3. **`ServicioComunicacionDepartamento.java`**
**Cambios Principales:**
- ❌ **ELIMINADO**: `reenviarVoto()` y métodos de votos
- ✅ **SIMPLIFICADO**: Solo `reenviarDelta()`
- ✅ **FOCO**: Comunicación exclusiva con servidor central

#### 4. **`ConfiguracionDepartamento.java`**
**Cambios Principales:**
- ✅ **AGREGADO**: `getDeltaUmbralVotos()`
- ✅ **AGREGADO**: `getDeltaUmbralTiempo()`
- ✅ **AGREGADO**: `getThreadPoolSize()`

### Configuración Externalizada

#### `departamento.properties`
```properties
# Map-Reduce Configuration
delta.umbral.votos=2000        # Umbral de votos para REDUCE
delta.umbral.tiempo=3000       # Umbral de tiempo para REDUCE (ms)
threads.pool.size=8            # Hilos para MAP phase

# Departamento Identity
nodo.id=DEPARTAMENTO-001
nodo.nombre=Departamento Central

# Connectivity
departamento.host=localhost
departamento.puerto=7001
servidor.central.host=localhost
servidor.central.puerto=6000
```

## Funcionalidades Eliminadas

### ❌ Código Legacy Removido
1. **Procesamiento de votos individuales**
2. **Validación de ciudadanos** (no corresponde al departamento)
3. **Almacenamiento local de votos**
4. **Conversión voto → delta** (ahora recibe deltas directamente)
5. **Logging verboso** (sistema silencioso para automación)

## Funcionalidades Mantenidas

### ✅ Funcionalidades Conservadas
1. **Verificación de conectividad** con servidor central
2. **Gestión de conexiones Ice**
3. **Configuración externalizada**
4. **Gestión ordenada de recursos** (thread pools, conexiones)

## Verificación de Implementación

### Compilación Exitosa
```bash
.\gradlew :departamento:build
# BUILD SUCCESSFUL in 5s
# 11 actionable tasks: 9 executed, 2 up-to-date
```

### Estructura de Clases
```
departamento/
├── controller/
│   └── DepartamentoController.java     # Map-Reduce orchestrator
├── deltas/
│   ├── RepositorioDeltas.java          # Thread-safe consolidation
│   └── GeneradorDeltas.java            # REDUCE phase scheduler
├── comunicacion/
│   ├── ServidorIceDepartamento.java    # Ice server (deltas only)
│   └── ServicioComunicacionDepartamento.java  # Central server comm
└── config/
    └── ConfiguracionDepartamento.java  # Externalized config
```

## Beneficios de la Refactorización

### 🚀 Performance
- **Paralelización**: MAP phase con thread pool configurable
- **Reducción de red**: Deltas consolidados vs votos individuales
- **Eficiencia**: Single-thread REDUCE phase para consistencia

### 🔒 Concurrencia
- **Thread Safety**: `ConcurrentHashMap` + `AtomicInteger`
- **Lock-Free**: Operaciones atómicas sin bloqueos explícitos
- **Scalability**: Pool de hilos dimensionable

### 🏗️ Arquitectura
- **Separación de responsabilidades**: MAP vs REDUCE phases
- **Callback pattern**: Desacoplamiento entre consolidación y envío
- **Configuration driven**: Umbrales y pools configurables

### 🔧 Mantenibilidad
- **Código limpio**: Eliminación de legacy code
- **Responsabilidad única**: Cada clase tiene un propósito específico
- **Testabilidad**: Componentes independientes y configurables

## Próximos Pasos

1. **Integración**: Conectar con lugares de votación refactorizados
2. **Testing**: Pruebas de carga con múltiples lugares
3. **Monitoring**: Métricas de consolidación y throughput
4. **Optimización**: Ajuste de umbrales según carga real

---

**Autor**: Sistema de Votación  
**Versión**: 2.0 - Map-Reduce Level 2  
**Fecha**: Junio 2025  
**Status**: ✅ IMPLEMENTADO Y COMPILADO
