# Diagrama de Deployment - Servidor Central

## Información General del Nodo

**Nombre del Nodo:** Servidor Central  
**Identificador:** SERVIDOR-CENTRAL-XXX (configurable)  
**Tipo:** Nodo de Consolidación Final (Reduce Final)  
**Runtime:** Java Application (JAR Ejecutable)  
**Ubicación de Deployment:** Servidor central nacional con alta capacidad de procesamiento  

---

## Arquitectura de Componentes de Deployment

### Componente Principal: `ServidorCentralApp`
- **Tipo:** Aplicación Principal ejecutable
- **Responsabilidad:** Punto de entrada, servidor ICE, coordinación y UI
- **Deployment:** JAR fat ejecutable (`servidor-central-fat.jar`)
- **Dependencias Externas:** 
  - Ice Framework para comunicaciones
  - Archivos de configuración y datos externos

---

## Componentes de Deployment Identificados

### 1. **Componente de Servidor ICE**
**Clases Involucradas:** `ServidorIceServidor`
- **Tipo:** ICE Server Middleware (Destino Final Map-Reduce)
- **Interfaces Provistas:**
  - `recibirDeltaConteo(DeltaConteo)` - Recepción de deltas departamentales
  - `ping()` - Verificación de disponibilidad del servidor
- **Interfaces NO Implementadas:**
  - `recibirValidacionVotante()` - Lanza UnsupportedOperationException (servidor final)
- **Protocolo de Comunicación:** TCP sobre ICE Framework
- **Endpoints Expuestos:**
  - Puerto 6000 (configurable) para recibir deltas departamentales
- **Dependencias de Deployment:**
  - ICE Framework libraries
  - Configuración de red (host/puerto del servidor)
- **Características de Deployment:**
  - **Destino Final**: No reenvía deltas a ningún otro nodo
  - **Alta Concurrencia**: Manejo simultáneo de múltiples departamentos

### 2. **Componente de Control de Negocio**
**Clases Involucradas:** `ServidorController`
- **Tipo:** Business Logic Controller (Reduce Final Controller)
- **Interfaces Provistas:**
  - `procesarDelta(DeltaConteo)` - Procesamiento de deltas departamentales
  - `obtenerConteoNacional()` - Acceso al conteo nacional consolidado
  - `obtenerTotalVotos()` - Total de votos nacionales
  - `hayVotos()` - Verificación de existencia de votos
  - `cerrarYGenerarReporte()` - Cierre ordenado y generación de CSV
- **Interfaces NO Implementadas:**
  - `validarVoto()` - Lanza UnsupportedOperationException (servidor final)
- **Interfaces Requeridas:**
  - ConsolidadorNacional (consolidación Map-Reduce)
  - GeneradorCSV (reportes finales)
  - ConfiguracionServidor (parámetros de configuración)
  - GestorCandidatos (información de candidatos)
- **Características de Deployment:**
  - **Thread Pool Configurable**: Procesamiento concurrente de deltas
  - **ExecutorService**: Gestión de hilos para Reduce Final
  - **Shutdown Hook**: Cierre limpio con generación de reportes

### 3. **Componente de Consolidación Nacional**
**Clases Involucradas:** `ConsolidadorNacional`
- **Tipo:** Map-Reduce Final Reducer
- **Interfaces Provistas:**
  - `consolidarDelta(DeltaConteo)` - Consolidación thread-safe de deltas
  - `obtenerConteoNacional()` - Conteo nacional inmutable
  - `obtenerTotalVotos()` - Total de votos consolidados
  - `hayVotos()` - Verificación de datos
  - `reiniciar()` - Reset para testing
- **Interfaces Requeridas:**
  - ConcurrentHashMap para thread-safety
  - AtomicInteger para contadores concurrentes
- **Características de Deployment:**
  - **Thread-Safe**: ConcurrentHashMap + AtomicInteger
  - **Reduce Final**: Consolidación de todos los deltas departamentales
  - **Almacenamiento en Memoria**: Conteo nacional persistente en RAM
  - **Alta Concurrencia**: Múltiples hilos consolidando simultáneamente

### 4. **Componente de Generación de Reportes**
**Clases Involucradas:** `GeneradorCSV`
- **Tipo:** Report Generation System
- **Interfaces Provistas:**
  - `generarReporte(Map<Integer, Integer>)` - Generación de CSV con nombres
  - `generarReporteVacio()` - Reporte cuando no hay datos
- **Interfaces Requeridas:**
  - GestorCandidatos (nombres de candidatos)
  - Sistema de archivos para escritura de CSV
- **Archivos Generados:**
  - `resumen.csv` (reporte nacional final)
- **Características de Deployment:**
  - **Formato CSV**: candidateId,candidateName,totalVotes
  - **Escape de Caracteres**: Manejo de comas en nombres
  - **Ordenamiento**: Por ID de candidato
  - **Estadísticas**: Porcentajes y resumen detallado

### 5. **Componente de Gestión de Candidatos**
**Clases Involucradas:** `GestorCandidatos`
- **Tipo:** Data Management Layer
- **Interfaces Provistas:**
  - `getCandidatoPorId(Integer)` - Búsqueda por ID
  - `getNombreCandidato(Integer)` - Nombre por ID con fallback
  - `getTodosCandidatos()` - Lista completa
  - `existeCandidato(Integer)` - Verificación de existencia
  - `getNumCandidatos()` - Conteo total
  - `getResumenCandidatos()` - Información para logging
- **Interfaces Requeridas:**
  - Sistema de archivos para lectura de CSV
  - BufferedReader para procesamiento de archivos
- **Archivos Externos de Deployment:**
  - `candidatos.csv` (junto al JAR)
- **Características de Deployment:**
  - **Carga en Memoria**: HashMap para acceso O(1)
  - **Validación de CSV**: Procesamiento robusto con manejo de errores
  - **Encoding UTF-8**: Soporte completo de caracteres especiales
  - **Fallback Graceful**: Nombres por defecto para IDs desconocidos

### 6. **Componente de Gestión de Configuración**
**Clases Involucradas:** `ConfiguracionServidor`
- **Tipo:** Configuration Management
- **Interfaces Provistas:**
  - **Propiedades del Servidor:**
    - `getServidorId()`, `getServidorNombre()` - Identidad del nodo
    - `getHost()`, `getPuerto()`, `getTimeout()` - Configuración de servidor ICE
  - **Propiedades de Thread Pool:**
    - `getThreadPoolSize()` - Tamaño del pool para Reduce Final
  - **Propiedades de Red:**
    - `getConexionReintentos()`, `getConexionTimeout()` - Conectividad
- **Interfaces Requeridas:**
  - Sistema de archivos para lectura de .properties
- **Archivos Externos de Deployment:**
  - `servidor-central.properties` (junto al JAR)
- **Características de Deployment:**
  - **Parsing Robusto**: `.trim()` para valores numéricos
  - **Configuración Crítica**: Thread pool para rendimiento de Reduce Final
  - **Validación Obligatoria**: Archivo debe existir para iniciar

### 7. **Componente de Interfaz de Usuario**
**Clases Involucradas:** `ServidorUI`
- **Tipo:** Console Management Interface
- **Interfaces Provistas:**
  - `iniciar()` - Bucle principal de UI
  - `mostrarEstadisticas()` - Visualización de conteo nacional
  - `mostrarMenu()` - Menú interactivo
  - `salir()` - Cierre con generación de reportes
- **Interfaces Requeridas:**
  - Scanner para entrada de usuario
  - System.out para salida de consola
  - ServidorController para acceso a datos
- **Características de Deployment:**
  - **Hilo Separado**: Ejecución en thread independiente
  - **Tiempo Real**: Actualización dinámica de estadísticas
  - **Cierre Controlado**: Generación de reportes al salir
  - **Interfaz Administrativa**: Monitoreo del sistema en ejecución

---

## Interfaces Externas del Nodo

### Interfaces Provistas (Servicios que Ofrece)

#### 1. **Interfaz ICE ReceptorVotos (Reduce Final)**
- **Protocolo:** TCP/Ice
- **Endpoint:** `servidor.host:servidor.puerto` (configurado en properties)
- **Operaciones Expuestas:**
  - `recibirDeltaConteo(DeltaConteo delta)` → boolean
    - **Propósito:** Recepción de deltas departamentales para consolidación final
    - **Comportamiento:** Envía delta a Thread Pool para procesamiento concurrente
    - **Destino Final:** NO reenvía a ningún otro nodo
  - `ping()` → boolean
    - **Propósito:** Verificación de disponibilidad del servidor central
- **Operaciones NO Soportadas:**
  - `recibirValidacionVotante()` - Lanza UnsupportedOperationException
- **Clientes Típicos:** Departamentos en la jerarquía Map-Reduce

### Interfaces Requeridas (Servicios que Consume)

#### 1. **Interfaz Sistema de Archivos**
- **Archivos Requeridos en Deployment:**
  - `servidor-central.properties` - Configuración completa del servidor
  - `candidatos.csv` - Lista de candidatos para reportes con nombres
- **Archivos Generados:**
  - `resumen.csv` - Reporte nacional final con resultados
- **Ubicación:** Directorio donde se ejecuta el JAR
- **Formato:** UTF-8 encoding para archivos CSV
- **Validación:** Archivos obligatorios para inicialización

#### 2. **Interfaz Usuario (Consola Administrativa)**
- **Tipo:** Standard Input/Output
- **Operaciones:**
  - Entrada: Scanner para comandos administrativos
  - Salida: System.out para estadísticas en tiempo real
- **Dependencias de Deployment:** JVM con soporte de consola interactiva
- **Funcionalidad:** Monitoreo y control administrativo del sistema

---

## Configuración de Deployment

### Archivos de Configuración Requeridos

#### `servidor-central.properties`
```properties
# Identidad del servidor central
nodo.id=SERVIDOR-CENTRAL-001
nodo.nombre=Servidor Central de Votacion

# Configuración del servidor ICE (destino final)
servidor.host=localhost
servidor.puerto=6000
servidor.timeout=5000

# Thread Pool para Reduce Final (crítico para rendimiento)
threads.pool.size=8

# Configuración de red
conexion.reintentos=3
conexion.timeout=3000

# Logging
logging.nivel=INFO
logging.archivo=servidor-central.log
```

#### `candidatos.csv`
```csv
# Formato: id,nombre,partido_politico
0,VOTO EN BLANCO,NINGUNO
1,Maria Elena Rodriguez,Partido Democratico Nacional
2,Carlos Alberto Mendoza,Alianza Popular
# ... más candidatos
```

### Artefactos de Deployment
- **JAR Principal:** `servidor-central-fat.jar`
- **Archivos de Configuración:** `servidor-central.properties`, `candidatos.csv`
- **Archivos Generados:** `resumen.csv` (reporte final nacional)
- **Logs:** `servidor-central.log`

---

## Dependencias de Runtime

### Framework y Bibliotecas
- **Java Runtime Environment (JRE) 8+**
- **Ice Framework** (ZeroC Ice para Java)
- **Bibliotecas estándar Java:**
  - java.util.concurrent (ExecutorService, ConcurrentHashMap)
  - java.util.concurrent.atomic (AtomicInteger)
  - java.io (File I/O operations)
  - java.util (Collections framework, Streams)

### Recursos del Sistema
- **Memoria:** Almacenamiento del conteo nacional completo
- **CPU:** Thread Pool configurable para procesamiento concurrente
- **Red:** Conectividad TCP para recibir deltas departamentales
- **Almacenamiento:** Lectura de configuración y generación de reportes
- **Consola:** Terminal administrativo para monitoreo

---

## Patrones de Comunicación

### 1. **Patrón Reduce Final (Map-Reduce Destination)**
- **Fase Reduce Final:** Consolidación de todos los deltas departamentales
- **Thread Pool:** Procesamiento concurrente de múltiples deltas
- **Agregación Nacional:** Suma de conteos por candidato a nivel país
- **Destino Terminal:** NO reenvía datos a ningún otro nodo

### 2. **Patrón Server (Receptor ICE)**
- **Rol:** Servidor que recibe deltas de múltiples departamentos
- **Protocolo:** Request-Response asíncrono (deltas enviados a Thread Pool)
- **Concurrencia:** ICE + Thread Pool para máximo rendimiento
- **Escalabilidad:** Pool configurable según carga de departamentos

### 3. **Patrón Repository (Consolidación Nacional)**
- **Almacenamiento:** ConcurrentHashMap para acceso thread-safe
- **Agregación:** AtomicInteger para incrementos concurrentes
- **Consistencia:** Operaciones atómicas para integridad de datos
- **Acceso:** Vistas inmutables para consultas seguras

### 4. **Patrón Observer (UI en Tiempo Real)**
- **Monitoreo:** UI consulta estado nacional dinámicamente
- **Tiempo Real:** Actualización de estadísticas sin bloqueo
- **Control Administrativo:** Cierre controlado con generación de reportes

---

## Consideraciones de Deployment

### Alta Disponibilidad
- **Destino Crítico**: Punto único de consolidación nacional
- **Thread Pool**: Procesamiento paralelo para alta carga
- **Almacenamiento en Memoria**: Acceso rápido a datos consolidados
- **Shutdown Graceful**: Cierre ordenado con preservación de datos

### Escalabilidad
- **Thread Pool Configurable**: Ajuste según número de departamentos
- **ConcurrentHashMap**: Escalabilidad para múltiples candidatos
- **Processing Paralelo**: Múltiples deltas procesados simultáneamente
- **Memory Efficiency**: Almacenamiento optimizado en estructuras concurrentes

### Rendimiento
- **Reduce Final Optimizado**: Agregación eficiente con AtomicInteger
- **Thread Pool Sizing**: Configuración crítica para rendimiento
- **Non-Blocking Operations**: Operaciones atómicas sin locks
- **Batch Processing**: Thread Pool evita bloqueos de procesamiento

### Seguridad y Auditoría
- **Conteo Nacional**: Consolidación completa para auditoría
- **Reportes con Nombres**: CSV final con información completa
- **Trazabilidad**: Logs de todos los deltas recibidos
- **Integridad**: Operaciones atómicas para consistencia de datos

### Mantenimiento
- **Configuración Externa**: Archivos .properties modificables
- **Thread Pool Tunable**: Ajuste de rendimiento sin recompilación
- **UI Administrativa**: Monitoreo en tiempo real del sistema
- **Reportes Automáticos**: Generación de CSV al cierre

### Monitoreo y Diagnóstico
- **Estadísticas en Tiempo Real**: Conteo nacional actualizado
- **UI Interactiva**: Visualización de estado del sistema
- **Pool de Threads**: Configuración visible para tuning
- **Reportes Detallados**: CSV con estadísticas completas

---

## Diagrama de Deployment Simplificado

```
[Servidor Central - JAR]
├── ICE Server (ServidorIceServidor)
│   └── ReceptorVotos Interface (Destino Final)
│       ├── recibirDeltaConteo() ✓ (Reduce Final)
│       ├── ping() ✓
│       └── recibirValidacionVotante() ✗ (UnsupportedOperation)
├── Business Logic (ServidorController)
│   └── Thread Pool (Reduce Final)
├── Consolidation (ConsolidadorNacional)
│   └── ConcurrentHashMap + AtomicInteger
├── Report Generation (GeneradorCSV)
├── Candidate Management (GestorCandidatos)
├── Configuration (ConfiguracionServidor)
└── Administrative UI (ServidorUI)
    └── Real-time National Statistics

[Interfaces Externas]
├── ICE Clients (Departamentos)
│   └── TCP/Ice Connection (puerto 6000)
│       └── Deltas departamentales → Reduce Final
├── File System
│   ├── servidor-central.properties (configuración)
│   ├── candidatos.csv (datos de candidatos)
│   └── resumen.csv (reporte final generado)
└── Administrative Console
    └── Real-time monitoring and control

[Flujo Reduce Final]
Departamentos → ICE Deltas → Thread Pool → ConsolidadorNacional → CSV Final
                                ↓
                         UI en Tiempo Real
```

Este análisis proporciona una vista completa de deployment del nodo Servidor Central, mostrando su función crítica como **destino final del sistema Map-Reduce**, consolidando todos los deltas departamentales en el conteo nacional definitivo, con capacidades de **procesamiento concurrente**, **monitoreo en tiempo real** y **generación de reportes finales** para la auditoría electoral.
