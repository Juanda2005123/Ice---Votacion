package comunicacion;

import VotingSystem.*;
import config.ConfiguracionLugar;

/**
 * Servicio para verificar conectividad con el broker destino y obtener información de nodos.
 * FUNCIÓN: Verificar que el broker lugar-departamento esté activo y obtener su identificación.
 */
public class ServicioVerificacionConectividad {
    
    private com.zeroc.Ice.Communicator communicator;
    
    public ServicioVerificacionConectividad() {
        try {
            this.communicator = com.zeroc.Ice.Util.initialize();
        } catch (Exception e) {
            throw new RuntimeException("Error inicializando communicator para verificación: " + e.getMessage());
        }
    }
      /**
     * Verifica la conectividad con el broker destino.
     * 
     * @param config La configuración del lugar que contiene los datos del broker destino
     * @return true si el broker destino responde al ping
     */
    public boolean verificarConectividad(ConfiguracionLugar config) {
        try {
            // Crear proxy al broker destino usando BrokerService (interfaz que implementan los brokers)
            String proxyString = String.format("BrokerService:tcp -h %s -p %d", 
                                             config.getBrokerDestinoHost(), 
                                             config.getBrokerDestinoPuerto());
            
            com.zeroc.Ice.ObjectPrx proxy = communicator.stringToProxy(proxyString);
            BrokerServicePrx brokerPrx = BrokerServicePrx.checkedCast(proxy);
            
            if (brokerPrx == null) {
                return false;
            }
            
            // Hacer ping para verificar conectividad
            return brokerPrx.ping();
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Verifica conectividad con el broker destino específico (consistente con broker)
     */
    public boolean verificarConectividadDestino(ConfiguracionLugar config) {
        return verificarConectividad(config);
    }
    
    /**
     * Obtiene información del broker destino si tiene una interfaz de información.
     * Por ahora solo verifica conectividad, pero se puede extender para obtener nodo.id y nodo.nombre.
     * 
     * @param config La configuración del lugar que contiene los datos del broker destino
     * @return Información del broker o null si no se puede obtener
     */
    public InfoNodo obtenerInfoNodo(ConfiguracionLugar config) {
        // Por ahora solo retornamos información básica si hay conectividad
        // En el futuro se podría implementar una interfaz Ice para obtener nodo.id y nodo.nombre
        if (verificarConectividad(config)) {
            String brokerInfo = config.getBrokerDestinoHost() + ":" + config.getBrokerDestinoPuerto();
            return new InfoNodo("BROKER-DESTINO", "Broker Lugar-Departamento", "broker", brokerInfo);
        }
        return null;
    }
    
    /**
     * Cierra el communicator
     */
    public void cerrar() {
        if (communicator != null) {
            communicator.destroy();
        }
    }
    
    /**
     * Clase para almacenar información de un nodo (consistente con la del broker)
     */
    public static class InfoNodo {
        private String id;
        private String nombre;
        private String tipo;
        private String ubicacion;
        
        public InfoNodo(String id, String nombre, String tipo, String ubicacion) {
            this.id = id;
            this.nombre = nombre;
            this.tipo = tipo;
            this.ubicacion = ubicacion;
        }
        
        public String getId() { return id; }
        public String getNombre() { return nombre; }
        public String getTipo() { return tipo; }
        public String getUbicacion() { return ubicacion; }
    }
}
