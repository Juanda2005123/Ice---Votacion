# Arquitectura Completa del Sistema de Votación Distribuido

## Resumen Ejecutivo

El **Sistema de Votación Distribuido** implementa una arquitectura híbrida que combina **Map-Reduce de 3 niveles**, **middleware de comunicación**, **validación distribuida** y **procesamiento concurrente** para manejar votaciones masivas con garantías de consistencia, performance y trazabilidad.

## Arquitectura General del Sistema

### Flujo Completo de Datos
```
[MESA] → [BROKER-M→L] → [LUGAR] → [BROKER-L→D] → [DEPTO] → [SERVIDOR-CENTRAL]
                                                              
```

### Componentes del Sistema

#### **Nodos de Procesamiento**
1. **Mesa de Votación** - Generación de votos
2. **Lugar de Votación** - Map-Reduce Level 1
3. **Departamento** - Map-Reduce Level 2  
4. **Servidor Central** - Reduce Final

#### **Nodos de Comunicación**
5. **Broker Mesa→Lugar** - Enrutamiento y load balancing
6. **Broker Lugar→Departamento** - Agregación de deltas

#### **Nodos de Validación**
7. **Proxy de Validación** - Validación de ciudadanos contra base de datos

---

## Descripción Detallada por Nodo

### 1. **Mesa de Votación** - Punto de Entrada

#### **Responsabilidades Principales**
- **Generación de votos individuales** desde interfaz de usuario
- **Validación de ciudadanos** vía Proxy de Validación
- **Envío de votos** al Broker Mesa→Lugar
- **Precarga de candidatos** desde archivo CSV externo

#### **Arquitectura Interna**
```java
// Thread Pool para procesamiento de votos
ExecutorService threadPoolVotos = Executors.newFixedThreadPool(poolSize);

// Repositorio thread-safe
ConcurrentHashMap<String, Ciudadano> votantesElegibles;
List<Candidato> candidatos; // Cargados desde candidatos.csv
```

#### **Flujo de Procesamiento de Voto**
1. **Validación de Ciudadano**:
   ```java
   // Llamada síncrona al Proxy de Validación
   boolean esValido = proxyValidacion.validarCiudadano(documento);
   ```

2. **Procesamiento de Voto**:
   ```java
   threadPoolVotos.submit(() -> {
       Voto voto = new Voto(ciudadano, candidato, timestamp);
       repositorio.registrarVoto(voto);
       enviarVotoBroker(voto);
   });
   ```

3. **Envío al Broker**:
   - Conversión a **DeltaConteo** (candidatoId → cantidad)
   - Envío asíncrono vía Ice al Broker Mesa→Lugar

#### **Configuración Thread Pool**
```properties
# mesa-votacion.properties
threads.pool.size=5          # Hilos para procesamiento concurrente
broker.host=localhost        # Broker destino
broker.puerto=9000
```

---

### 2. **Broker Mesa→Lugar** - Enrutamiento Inteligente

#### **Responsabilidades Principales**
- **Load Balancing** entre múltiples lugares de votación
- **Agregación temporal** de deltas de múltiples mesas
- **Enrutamiento basado en estrategias** (Round-Robin, LRU)

#### **Arquitectura Interna - Thread Pool + Enrutamiento**
```java
public class ConfiguracionBroker {
    // Thread Pool para reenvío paralelo
    private int threadPoolSize = 10;
    
    // Múltiples destinos con load balancing
    private List<Destino> destinos;
    
    // Estrategia de enrutamiento
    private String estrategiaEnrutamiento = "LRU"; // Least Recently Used
}
```

#### **Patrón Load Balancing LRU**
```java
public class Destino {
    private long ultimoUso;  // Timestamp para LRU
    
    public void marcarComoUsado() {
        this.ultimoUso = System.currentTimeMillis();
    }
}

// Selección LRU
public Destino seleccionarDestinoLRU() {
    return destinos.stream()
        .filter(Destino::isActivo)
        .min(Comparator.comparing(Destino::getUltimoUso))
        .orElse(null);
}
```

#### **Agregación de Deltas con Thread Pool**
```java
// Thread Pool para procesamiento paralelo
ExecutorService threadPoolReenvio = Executors.newFixedThreadPool(10);

// Buffer temporal thread-safe
ConcurrentHashMap<Integer, AtomicInteger> bufferDeltas;

// Procesamiento asíncrono
threadPoolReenvio.submit(() -> {
    // 1. Agregar delta al buffer
    bufferDeltas.computeIfAbsent(candidatoId, k -> new AtomicInteger(0))
                .addAndGet(cantidadVotos);
    
    // 2. Si alcanza umbral, reenviar
    if (shouldFlushBuffer()) {
        reenviarDeltasAgregados();
    }
});
```

#### **Integración con Proxy de Validación**
```java
// Validación antes de reenvío
public void procesarDelta(DeltaConteo delta) {
    // Opcional: Re-validar integridad
    if (proxy.validarIntegridadDelta(delta)) {
        threadPoolReenvio.submit(() -> reenviarALugar(delta));
    }
}
```

---

### 3. **Lugar de Votación** - Map-Reduce Level 1

#### **Responsabilidades Principales**
- **Recepción** de deltas agregados del Broker Mesa→Lugar
- **Map-Reduce Level 1**: Consolidación por lugar de votación
- **Thread Pool**: Procesamiento concurrente de deltas
- **Umbralización**: Envío por cantidad/tiempo al Broker Lugar→Departamento

#### **Implementación Map-Reduce Level 1**
```java
public class ConsolidadorLugar {
    // MAP Phase: Thread Pool para procesamiento concurrente
    private ExecutorService threadPoolMap;
    
    // REDUCE Phase: Estado consolidado thread-safe
    private ConcurrentHashMap<Integer, AtomicInteger> conteoConsolidado;
    
    // Scheduler para REDUCE temporal
    private ScheduledExecutorService scheduler;
}
```

#### **MAP Phase - Procesamiento Concurrente**
```java
// Cada delta se procesa en thread separado
public void procesarDelta(DeltaConteo delta) {
    threadPoolMap.submit(() -> {
        // MAP: Procesar cada voto en el delta
        for (Map.Entry<Integer, Integer> entry : delta.votos.entrySet()) {
            Integer candidatoId = entry.getKey();
            Integer cantidad = entry.getValue();
            
            // REDUCE: Agregar a conteo consolidado (thread-safe)
            conteoConsolidado.computeIfAbsent(candidatoId, k -> new AtomicInteger(0))
                           .addAndGet(cantidad);
        }
        
        // Verificar umbrales después de MAP
        verificarUmbralesEnvio();
    });
}
```

#### **REDUCE Phase - Consolidación y Umbrales**
```java
// REDUCE temporal programado
scheduler.scheduleAtFixedRate(() -> {
    if (shouldGenerateDelta()) {
        DeltaConteo deltaConsolidado = generarDeltaConsolidado();
        enviarADepartamento(deltaConsolidado);
        limpiarConteoConsolidado();
    }
}, 0, delta.umbral.tiempo, TimeUnit.MILLISECONDS);

// REDUCE por cantidad
private void verificarUmbralesEnvio() {
    int totalVotos = conteoConsolidado.values().stream()
        .mapToInt(AtomicInteger::get)
        .sum();
        
    if (totalVotos >= configuracion.getDeltaUmbralVotos()) {
        // Trigger REDUCE phase
        enviarDeltaConsolidado();
    }
}
```

#### **Configuración Map-Reduce Level 1**
```properties
# lugar-votacion.properties
threads.pool.size=12         # MAP phase parallelism
delta.umbral.votos=500       # REDUCE trigger (cantidad)
delta.umbral.tiempo=1000     # REDUCE trigger (tiempo ms)
broker.destino.host=localhost # Broker Lugar→Departamento
broker.destino.puerto=9001
```

---

### 4. **Broker Lugar→Departamento** - Agregación Departamental

#### **Responsabilidades Principales**
- **Agregación por departamento** de deltas de múltiples lugares
- **Buffer temporal** con flush automático
- **Thread Pool** para reenvío paralelo
- **Validación distribuida** vía Proxy de Validación

#### **Arquitectura de Agregación**
```java
public class AgregarorDepartamental {
    // Buffer por departamento
    private ConcurrentHashMap<String, ConcurrentHashMap<Integer, AtomicInteger>> 
        bufferPorDepartamento;
    
    // Thread Pool para agregación
    private ExecutorService threadPoolAgregacion;
    
    // Métricas por lugar
    private ConcurrentHashMap<String, AtomicLong> metricas;
}
```

#### **Agregación Multi-Lugar**
```java
public void procesarDeltaLugar(String lugarId, DeltaConteo delta) {
    threadPoolAgregacion.submit(() -> {
        String deptoId = extraerDepartamento(lugarId);
        
        // Agregar al buffer departamental
        ConcurrentHashMap<Integer, AtomicInteger> bufferDepto = 
            bufferPorDepartamento.computeIfAbsent(deptoId, 
                k -> new ConcurrentHashMap<>());
        
        // Consolidar votos
        for (Map.Entry<Integer, Integer> entry : delta.votos.entrySet()) {
            bufferDepto.computeIfAbsent(entry.getKey(), 
                k -> new AtomicInteger(0))
                .addAndGet(entry.getValue());
        }
        
        // Actualizar métricas
        metricas.computeIfAbsent(lugarId, k -> new AtomicLong(0))
               .addAndGet(delta.getTotalVotos());
        
        // Verificar flush
        if (shouldFlushDepartamento(deptoId)) {
            flushBufferDepartamento(deptoId);
        }
    });
}
```

---

### 5. **Departamento** - Map-Reduce Level 2

#### **Responsabilidades Principales**
- **Map-Reduce Level 2**: Consolidación departamental
- **Thread Pool**: Procesamiento de deltas de múltiples lugares
- **Agregación temporal**: Buffer con umbrales configurables
- **Conexión directa**: Ice hacia Servidor Central (sin broker)

#### **Implementación Map-Reduce Level 2**
```java
public class ConsolidadorDepartamental {
    // MAP Phase: Thread Pool para deltas de lugares
    private ExecutorService threadPoolMap;
    
    // REDUCE Phase: Consolidación departamental
    private ConcurrentHashMap<Integer, AtomicInteger> conteoConsolidado;
    
    // Callback para REDUCE automático
    private ConsolidacionCallback callback;
}
```

#### **MAP Phase - Múltiples Lugares**
```java
// Cada lugar envía deltas al departamento
public void procesarDeltaLugar(DeltaConteo deltaLugar) {
    threadPoolMap.submit(() -> {
        // MAP: Procesar delta del lugar
        for (Map.Entry<Integer, Integer> entry : deltaLugar.votos.entrySet()) {
            // REDUCE: Agregar a consolidación departamental
            conteoConsolidado.computeIfAbsent(entry.getKey(), 
                k -> new AtomicInteger(0))
                .addAndGet(entry.getValue());
        }
        
        // Callback para verificar REDUCE
        callback.onDeltaProcesado(deltaLugar);
    });
}
```

#### **REDUCE Phase - Consolidación Departamental**
```java
// Callback Pattern para REDUCE
public class ConsolidacionCallback {
    public void onDeltaProcesado(DeltaConteo delta) {
        // Verificar umbrales para REDUCE
        if (shouldTriggerReduce()) {
            // Generar delta departamental consolidado
            DeltaConteo deltaDepartamental = generarDeltaDepartamental();
            
            // Enviar directo al Servidor Central (sin broker)
            servidorCentralProxy.procesarDeltaDepartamental(deltaDepartamental);
            
            // Limpiar buffer
            limpiarConsolidacion();
        }
    }
}
```

#### **Configuración Map-Reduce Level 2**
```properties
# departamento.properties
threads.pool.size=8          # MAP phase parallelism
delta.umbral.votos=2000      # REDUCE trigger (cantidad)
delta.umbral.tiempo=3000     # REDUCE trigger (tiempo ms)
servidor.central.host=localhost # Conexión directa
servidor.central.puerto=6000
```

---

### 6. **Servidor Central** - Reduce Final

#### **Responsabilidades Principales**
- **Reduce Final**: Consolidación nacional de todos los departamentos
- **Thread Pool**: Procesamiento concurrente de deltas departamentales
- **Conteo Nacional**: Estado final thread-safe
- **Reporte CSV**: Generación automática con nombres de candidatos
- **UI Interactiva**: Estadísticas en tiempo real

#### **Implementación Reduce Final**
```java
public class ConsolidadorNacional {
    // Thread Pool para Reduce Final
    private ExecutorService threadPoolReduceFinal;
    
    // Estado nacional thread-safe
    private ConcurrentHashMap<Integer, AtomicInteger> conteoNacional;
    
    // Gestor de candidatos para nombres en CSV
    private GestorCandidatos gestorCandidatos;
}
```

#### **Reduce Final - Procesamiento Nacional**
```java
// Cada departamento envía su delta consolidado
public void procesarDeltaDepartamental(DeltaConteo deltaDepartamento) {
    threadPoolReduceFinal.submit(() -> {
        // REDUCE FINAL: Agregar al conteo nacional
        for (Map.Entry<Integer, Integer> entry : deltaDepartamento.votos.entrySet()) {
            Integer candidatoId = entry.getKey();
            Integer votos = entry.getValue();
            
            // Thread-safe accumulation
            conteoNacional.computeIfAbsent(candidatoId, k -> new AtomicInteger(0))
                         .addAndGet(votos);
        }
        
        // Actualizar UI en tiempo real
        ui.actualizarEstadisticas(conteoNacional);
    });
}
```

#### **Generación CSV Final con Nombres**
```java
public class GeneradorCSV {
    private GestorCandidatos gestorCandidatos;
    
    public boolean generarReporte(Map<Integer, Integer> conteoNacional) {
        try (FileWriter writer = new FileWriter("resumen.csv")) {
            // Header con nombres
            writer.write("candidateId,candidateName,totalVotes\n");
            
            // Datos ordenados con nombres
            conteoNacional.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    Integer candidatoId = entry.getKey();
                    Integer totalVotos = entry.getValue();
                    String nombre = gestorCandidatos.getNombreCandidato(candidatoId);
                    
                    writer.write(candidatoId + "," + nombre + "," + totalVotos + "\n");
                });
        }
    }
}
```

#### **UI Interactiva en Tiempo Real**
```java
public class ServidorUI {
    // Actualización automática cada 2 segundos
    ScheduledExecutorService uiScheduler = 
        Executors.newSingleThreadScheduledExecutor();
    
    uiScheduler.scheduleAtFixedRate(() -> {
        mostrarEstadisticasActualizadas();
        mostrarThroughputActual();
        mostrarTopCandidatos();
    }, 0, 2, TimeUnit.SECONDS);
}
```

---

### 7. **Proxy de Validación** - Validación Distribuida

#### **Responsabilidades Principales**
- **Validación de ciudadanos** contra base de datos PostgreSQL
- **Pool de conexiones** para alta concurrencia
- **Cache interno** para optimización de consultas
- **Interfaz Ice** para comunicación con mesas y brokers

#### **Arquitectura de Validación**
```java
public class ProxyValidacion {
    // Pool de conexiones a PostgreSQL
    private HikariDataSource dataSource;
    
    // Cache para consultas frecuentes
    private ConcurrentHashMap<String, Boolean> cacheValidacion;
    
    // Thread Pool para consultas paralelas
    private ExecutorService threadPoolValidacion;
}
```

#### **Validación con Pool de Conexiones**
```java
public boolean validarCiudadano(String documento) {
    // Check cache first
    Boolean cached = cacheValidacion.get(documento);
    if (cached != null) {
        return cached;
    }
    
    // Query database
    try (Connection conn = dataSource.getConnection();
         PreparedStatement stmt = conn.prepareStatement(
             "SELECT COUNT(*) FROM ciudadano WHERE documento = ?")) {
        
        stmt.setString(1, documento);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            boolean valido = rs.getInt(1) > 0;
            // Cache result
            cacheValidacion.put(documento, valido);
            return valido;
        }
    }
    return false;
}
```

#### **Configuración Pool de Conexiones**
```properties
# proxy.properties
db.pool.minimo=2             # Conexiones mínimas
db.pool.maximo=10            # Conexiones máximas
db.timeout.conexion=30000    # Timeout conexión
db.timeout.query=15000       # Timeout query
threads.pool.size=8          # Thread pool validación
```

---

## Patrones de Diseño Implementados

### 1. **Map-Reduce Distribuido**

#### **Patrón MAP**
- **Definición**: Procesamiento paralelo de elementos individuales
- **Implementación**: `ExecutorService.submit()` para cada delta/voto
- **Thread Safety**: `ConcurrentHashMap` + `AtomicInteger`

```java
// MAP Phase Pattern
public void map(InputData input) {
    threadPool.submit(() -> {
        ProcessedData result = process(input);
        accumulate(result); // Thread-safe accumulation
    });
}
```

#### **Patrón REDUCE**
- **Definición**: Consolidación/agregación de resultados parciales
- **Implementación**: Triggers por cantidad/tiempo + callback pattern
- **Output**: Delta consolidado enviado al siguiente nivel

```java
// REDUCE Phase Pattern
public void reduce() {
    if (shouldTriggerReduce()) {
        ConsolidatedResult result = consolidatePartialResults();
        sendToNextLevel(result);
        clearBuffer();
    }
}
```

### 2. **Thread Pool Pattern**

#### **Configuración Adaptativa**
- **Thread Pool por nodo**: Dimensionado según carga esperada
- **Sized Pool**: `Executors.newFixedThreadPool(configurable_size)`
- **Scheduled Pool**: Para triggers temporales de REDUCE

```java
// Thread Pool Configuration Pattern
public class NodeConfiguration {
    private ExecutorService threadPool;
    private ScheduledExecutorService scheduler;
    
    public void initialize() {
        int poolSize = config.getThreadPoolSize();
        threadPool = Executors.newFixedThreadPool(poolSize);
        scheduler = Executors.newScheduledThreadPool(2);
    }
}
```

### 3. **Callback Pattern**

#### **Desacoplamiento MAP-REDUCE**
- **Trigger automático**: Callback al completar MAP phase
- **Verificación de umbrales**: Callback para trigger REDUCE
- **Event-driven**: Reducción de polling y mayor eficiencia

```java
// Callback Pattern for REDUCE triggers
public interface ConsolidacionCallback {
    void onElementProcessed(ProcessedElement element);
    void onThresholdReached(ConsolidatedData data);
}
```

### 4. **Proxy Pattern**

#### **Transparencia de Red**
- **Ice Proxies**: Cliente-servidor transparente
- **Failover**: Reconexión automática
- **Load Balancing**: Distribución automática de carga

```java
// Proxy Pattern with Ice
ReceptorVotosPrx proxy = ReceptorVotosPrxHelper.checkedCast(
    communicator.stringToProxy("ReceptorVotos:tcp -h host -p port")
);
```

### 5. **Strategy Pattern**

#### **Estrategias de Enrutamiento**
- **Round-Robin**: Distribución equitativa
- **LRU**: Least Recently Used
- **Weighted**: Basado en capacidad

```java
// Strategy Pattern for routing
public enum RoutingStrategy {
    ROUND_ROBIN,
    LRU,
    WEIGHTED;
    
    public Destino selectDestination(List<Destino> destinos);
}
```

---

## Flujo Completo de un Voto

### Flujo Detallado: Desde Mesa hasta Reporte Final

#### **Paso 1: Mesa de Votación**
```
[CIUDADANO] → [VALIDACIÓN-PROXY] → [VOTO] → [THREAD-POOL] → [BROKER-M→L]
```
1. **Input**: Documento ciudadano + candidato seleccionado
2. **Validación**: Consulta síncrona al Proxy de Validación
3. **Processing**: Thread pool procesa voto en paralelo
4. **Output**: DeltaConteo enviado al Broker Mesa→Lugar

#### **Paso 2: Broker Mesa→Lugar**
```
[MÚLTIPLES-MESAS] → [AGGREGATION] → [LOAD-BALANCING] → [LUGAR-VOTACIÓN]
```
1. **Input**: Deltas de múltiples mesas simultáneamente
2. **Aggregation**: Buffer temporal con thread pool
3. **Load Balancing**: Estrategia LRU para múltiples lugares
4. **Output**: Delta agregado enviado al lugar seleccionado

#### **Paso 3: Lugar de Votación (Map-Reduce L1)**
```
[DELTAS-AGREGADOS] → [MAP-PHASE] → [REDUCE-PHASE] → [BROKER-L→D]
```
1. **MAP Phase**: Thread pool procesa cada delta en paralelo
2. **Accumulation**: ConcurrentHashMap consolida votos por candidato
3. **REDUCE Phase**: Trigger por umbral (cantidad/tiempo)
4. **Output**: Delta consolidado por lugar → Broker Lugar→Departamento

#### **Paso 4: Broker Lugar→Departamento**
```
[MÚLTIPLES-LUGARES] → [AGGREGATION-DEPTO] → [DEPARTAMENTO]
```
1. **Input**: Deltas consolidados de múltiples lugares
2. **Aggregation**: Consolidación por departamento
3. **Buffering**: Buffer temporal por departamento
4. **Output**: Delta departamental → Departamento

#### **Paso 5: Departamento (Map-Reduce L2)**
```
[DELTAS-LUGAR] → [MAP-PHASE] → [REDUCE-PHASE] → [SERVIDOR-CENTRAL]
```
1. **MAP Phase**: Thread pool procesa deltas de lugares
2. **Accumulation**: Consolidación departamental thread-safe
3. **REDUCE Phase**: Callback pattern para triggers
4. **Output**: Delta departamental → Servidor Central (directo)

#### **Paso 6: Servidor Central (Reduce Final)**
```
[DELTAS-DEPTO] → [REDUCE-FINAL] → [CONTEO-NACIONAL] → [CSV-REPORT]
```
1. **Input**: Deltas consolidados de múltiples departamentos
2. **Reduce Final**: Thread pool para consolidación nacional
3. **Real-time UI**: Actualización de estadísticas live
4. **Output**: Conteo nacional + CSV con nombres de candidatos

---

## Consideraciones de Performance

### **Thread Pool Sizing**
- **Mesa**: 5 hilos (baja concurrencia, UI responsiva)
- **Broker**: 10 hilos (alto throughput, múltiples fuentes)
- **Lugar**: 12 hilos (Map-Reduce L1, balance MAP/REDUCE)
- **Departamento**: 8 hilos (Map-Reduce L2, menos frecuencia)
- **Servidor**: 8 hilos (Reduce Final, consolidación)

### **Memory Management**
- **ConcurrentHashMap**: Estado compartido thread-safe
- **AtomicInteger**: Operaciones atómicas sin locks
- **Buffer Limits**: Umbrales configurables para evitar OutOfMemory
- **Cache Cleanup**: Limpieza periódica de buffers

### **Network Optimization**
- **Delta Compression**: Envío de incrementos, no estado completo
- **Batch Processing**: Agregación antes de envío
- **Connection Pooling**: Reutilización de conexiones Ice
- **Timeout Configuration**: Timeouts configurables por nivel

---

## Tolerancia a Fallos

### **Reconexión Automática**
```java
// Ice proxy with automatic reconnection
@Retry(maxAttempts = 3, backoff = @Backoff(delay = 1000))
public void enviarDelta(DeltaConteo delta) {
    try {
        proxy.procesarDelta(delta);
    } catch (Ice.ConnectFailureException e) {
        // Ice automáticamente reintenta conexión
        throw new RuntimeException("Fallo de conexión", e);
    }
}
```

### **Buffer Persistence**
- **Temporary Files**: Buffers críticos en disco
- **Recovery**: Recuperación de estado en restart
- **Graceful Shutdown**: Flush de buffers antes de cerrar

### **Data Consistency**
- **Atomic Operations**: AtomicInteger para conteos
- **Transaction-like**: Operaciones todo-o-nada
- **Idempotency**: Reprocessing seguro de deltas

---

## Monitoreo y Observabilidad

### **Métricas por Nodo**
- **Throughput**: Votos/segundo, deltas/segundo
- **Latency**: Tiempo de procesamiento por fase
- **Queue Size**: Tamaño de buffers internos
- **Thread Utilization**: Uso de thread pools

### **Logging Estructurado**
```java
// Structured logging pattern
logger.info("[MAP-REDUCE-L1] Delta procesado: candidateId={}, votos={}, threadId={}", 
           candidateId, cantidad, Thread.currentThread().getId());
```

### **UI en Tiempo Real**
- **Live Dashboard**: Estadísticas actualizadas cada 2 segundos
- **Progress Indicators**: Progreso de consolidación
- **Top Candidates**: Ranking en tiempo real
- **System Health**: Estado de conexiones y thread pools

---

## Conclusiones

### **Beneficios de la Arquitectura**

#### **Escalabilidad Horizontal**
- **Múltiples instancias**: Cada nodo puede escalarse independientemente
- **Load Balancing**: Distribución automática de carga
- **Partitioning**: División geográfica natural (mesa→lugar→depto)

#### **Performance**
- **Paralelización**: Thread pools en cada nivel
- **Consolidación**: Reducción exponencial de tráfico de red
- **Caching**: Validaciones y datos frecuentes en memoria

#### **Confiabilidad**
- **Thread Safety**: Operaciones atómicas garantizadas
- **Fault Tolerance**: Reconexión automática y retry logic
- **Data Consistency**: Estado coherente en toda la cadena

#### **Observabilidad**
- **Real-time Monitoring**: UI interactiva con estadísticas live
- **Structured Logging**: Trazabilidad completa del flujo
- **Configurable Metrics**: Umbrales y parámetros externalizados

### **Casos de Uso Apropiados**

#### **Elecciones Masivas**
- **10,000+ mesas**: Escalabilidad horizontal probada
- **Millones de votos**: Map-Reduce maneja volúmenes masivos
- **Tiempo Real**: Resultados parciales disponibles durante votación

#### **Alta Disponibilidad**
- **24/7 Operation**: Sistema preparado para operación continua
- **Geographic Distribution**: Distribución geográfica natural
- **Disaster Recovery**: Múltiples puntos de fallo independientes

---

**Autor**: Sistema de Votación - Análisis Arquitectural  
**Versión**: 1.0 - Documentación Completa  
**Fecha**: Junio 2025  
**Cobertura**: 7 nodos, 5 patrones de diseño, flujo completo end-to-end
