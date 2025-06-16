# Broker Node - Interface Analysis & Deployment Components

## Overview

This document provides a detailed analysis of the interfaces provided and required by the **Broker** node in the distributed voting system, showing the deployment-level components and their interactions.

## Provided Interfaces (Services Exposed by Broker)

### 1. BrokerService Interface
- **Interface Type**: Ice Interface (generated from Slice)
- **Protocol**: Ice over TCP
- **Port**: Configurable via `broker.puerto` (default: 9000)
- **Identity**: `"BrokerService"`
- **Implemented By**: `ServidorIceBroker` class

#### Methods Provided:
```java
// Delta processing and forwarding
boolean recibirDeltaConteo(DeltaConteo delta, com.zeroc.Ice.Current current)

// Voter validation forwarding
int recibirValidacionVotante(String documento, int candidatoId, com.zeroc.Ice.Current current)

// Health check endpoint
boolean ping(com.zeroc.Ice.Current current)
```

#### Service Implementation:
- **Component**: `ServidorIceBroker`
- **Responsibility**: Receives requests from upstream nodes (mesa-votacion)
- **Processing**: Delegates to `BrokerController` with thread pool processing
- **Load Balancing**: Transparent to clients (handled internally)

## Required Interfaces (External Services Used by Broker)

### 1. ReceptorVotos Interface (to Destinations)
- **Interface Type**: Ice Interface (generated from Slice)
- **Protocol**: Ice over TCP
- **Used By**: `ServicioComunicacionBroker`
- **Purpose**: Forward deltas and validations to destination nodes

#### Methods Used:
```java
// Forward consolidated deltas to destinations
boolean recibirDeltaConteo(DeltaConteo delta)

// Forward voter validation requests
int recibirValidacionVotante(String documento, int candidatoId)

// Health check destinations
boolean ping()
```

#### Target Nodes:
- **Lugar Votación**: Connection via `destino.X.host:destino.X.puerto`
- **Departamento**: Connection via `destino.X.host:destino.X.puerto`
- **Configuration**: Multiple destinations configured in `broker.properties`

### 2. ReceptorVotos Interface (to Proxy Validation)
- **Interface Type**: Ice Interface (generated from Slice)
- **Protocol**: Ice over TCP
- **Used By**: `ServicioComunicacionBroker.enviarValidacionAProxy()`
- **Purpose**: Forward voter validation to centralized validation service

#### Connection Details:
- **Host**: Configured via `proxy.host`
- **Port**: Configured via `proxy.puerto` (default: 11001)
- **Identity**: `"ReceptorVotos"`
- **Method Used**: `recibirValidacionVotante(String documento, int candidatoId)`

## Internal Component Architecture

### 1. BrokerController (Business Logic)
- **Type**: Business logic controller
- **Threading**: Uses `ExecutorService` thread pool
- **Responsibilities**:
  - Delta processing coordination
  - Voter validation orchestration
  - Destination health management
  - Resource lifecycle management

#### Key Dependencies:
```java
- ServicioComunicacionBroker: Outbound communication
- ServicioVerificacionConectividad: Health monitoring
- EstrategiaEnrutamiento: Load balancing logic
- ExecutorService: Parallel processing (configurable pool size)
```

### 2. ServicioComunicacionBroker (Communication Layer)
- **Type**: Outbound communication service
- **Ice Client**: Creates proxies to destination services
- **Load Balancing**: Uses `EstrategiaEnrutamiento` for destination selection
- **Reliability**: Handles connection failures and retries

#### External Connections:
```java
// Multiple destination connections (lugar-votacion, departamento)
ReceptorVotosPrx destinationProxy = ReceptorVotosPrx.checkedCast(proxy);

// Proxy validation connection
ReceptorVotosPrx proxyValidationPrx = ReceptorVotosPrx.checkedCast(proxy);
```

### 3. EstrategiaEnrutamiento (Load Balancing)
- **Type**: Load balancing strategy
- **Algorithm**: LRU (Least Recently Used)
- **Health Awareness**: Real-time connectivity checking
- **State Management**: Tracks last usage time per destination

#### Destination Selection Logic:
```java
1. Get all active destinations from configuration
2. Filter by real-time connectivity (ServicioVerificacionConectividad)
3. Apply LRU algorithm to connected destinations
4. Update usage timestamp for selected destination
5. Return selected destination for forwarding
```

### 4. ServicioVerificacionConectividad (Health Monitoring)
- **Type**: Connectivity verification service
- **Ice Client**: Creates temporary proxies for health checks
- **Real-time**: Checks connectivity before each forwarding operation
- **Interface Used**: `ReceptorVotos.ping()` method

## Interface Flow Diagrams

### Delta Processing Flow
```
Mesa Votación
    ↓ (Ice TCP)
[BrokerService.recibirDeltaConteo()]
    ↓
BrokerController.procesarDelta()
    ↓ (Thread Pool)
ServicioComunicacionBroker.reenviarDelta()
    ↓
EstrategiaEnrutamiento.seleccionarDestino() → LRU Selection
    ↓
ServicioVerificacionConectividad.verificarConectividad() → Health Check
    ↓ (Ice TCP)
[ReceptorVotos.recibirDeltaConteo()] → Destination Node
```

### Voter Validation Flow
```
Mesa Votación
    ↓ (Ice TCP)
[BrokerService.recibirValidacionVotante()]
    ↓
BrokerController.validarCiudadano()
    ↓
ServicioComunicacionBroker.enviarValidacionAProxy()
    ↓ (Ice TCP)
[ReceptorVotos.recibirValidacionVotante()] → Proxy Validation Service
    ↓
Return validation code (1=valid, 3=invalid, 4=error)
```

## Configuration-Driven Interface Binding

### Destination Configuration
```properties
# Dynamic destination binding
destinos.cantidad=2

# Destination 1 interface binding
destino.1.id=LUGAR-001
destino.1.host=localhost
destino.1.puerto=8001
destino.1.activo=true

# Destination 2 interface binding  
destino.2.id=LUGAR-002
destino.2.host=localhost
destino.2.puerto=8002
destino.2.activo=true
```

### Proxy Validation Configuration
```properties
# Centralized validation interface binding
proxy.host=localhost
proxy.puerto=11001
proxy.timeout=5000
```

### Service Binding Configuration
```properties
# Inbound service binding
broker.host=localhost
broker.puerto=9000
broker.timeout=5000

# Thread pool for interface processing
threads.pool.size=10
```

## Interface Dependencies Matrix

| Component | Provides Interface | Requires Interface | Target Node | Protocol |
|-----------|-------------------|-------------------|-------------|----------|
| ServidorIceBroker | BrokerService | - | - | Ice TCP |
| ServicioComunicacionBroker | - | ReceptorVotos | Lugar/Departamento | Ice TCP |
| ServicioComunicacionBroker | - | ReceptorVotos | Proxy Validation | Ice TCP |
| ServicioVerificacionConectividad | - | ReceptorVotos.ping() | All Destinations | Ice TCP |
| EstrategiaEnrutamiento | - | - (uses connectivity service) | - | Internal |

## Interface Security & Reliability

### Security Considerations
- **No Authentication**: Relies on network-level security
- **No Encryption**: Plain TCP communication
- **Trust Model**: Internal network deployment assumed
- **Access Control**: Network firewall rules recommended

### Reliability Features
- **Health Monitoring**: Real-time destination availability checks
- **Load Balancing**: LRU distribution prevents overload
- **Connection Pooling**: Ice framework manages connection reuse
- **Timeout Handling**: Configurable timeouts for all interface calls
- **Graceful Degradation**: Continues operating with reduced destinations

### Error Handling
- **Connection Failures**: Automatic exclusion from destination pool
- **Processing Errors**: Isolated thread processing prevents cascading failures
- **Network Timeouts**: Configurable timeout values per interface
- **Resource Cleanup**: Proper Ice proxy and connection management

## Interface Performance Characteristics

### Throughput Optimization
- **Parallel Processing**: Thread pool handles multiple requests simultaneously
- **Connection Reuse**: Ice proxy caching minimizes connection overhead
- **Load Distribution**: LRU ensures even workload distribution
- **Asynchronous Processing**: Non-blocking request handling

### Scalability Factors
- **Destination Scaling**: Dynamic addition/removal of destinations
- **Thread Pool Tuning**: Configurable pool size for hardware optimization
- **Memory Management**: Efficient proxy and connection lifecycle
- **Network Optimization**: Ice protocol efficiency for high-throughput scenarios

## Deployment Interface Validation

### Startup Interface Verification
1. **Configuration Loading**: Validate all destination configurations
2. **Ice Communicator**: Initialize Ice runtime and object adapter
3. **Service Binding**: Register BrokerService with configured identity
4. **Destination Health**: Verify connectivity to all configured destinations
5. **Proxy Validation**: Test connection to validation proxy service
6. **Load Balancer**: Initialize LRU state and destination tracking

### Runtime Interface Monitoring
- **Health Checks**: Continuous destination availability monitoring
- **Load Balancing**: LRU algorithm execution and destination selection
- **Connection Management**: Ice proxy lifecycle and cleanup
- **Error Recovery**: Automatic destination re-inclusion when recovered

This interface analysis provides the deployment-level view of the broker's integration points, showing exactly which interfaces are provided, required, and how they interconnect with other system components for effective distributed voting system operation.
