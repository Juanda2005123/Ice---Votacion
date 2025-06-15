package comunicacion;

import VotingSystem.*;
import config.ConfiguracionDepartamento;

/** * Servicio para verificar conectividad con el broker destino y obtener informacion de nodos.
 * Esta clase es responsable de verificar que el broker lugar-departamento este activo y obtener su identificacion.
 */
public class ServicioVerificacionConectividad {
    
    private com.zeroc.Ice.Communicator communicator;
    
    public ServicioVerificacionConectividad() {
        try {
            this.communicator = com.zeroc.Ice.Util.initialize();
        } catch (Exception e) {
            throw new RuntimeException("Error inicializando communicator para verificacion: " + e.getMessage());
        }
    }    /**
     * Verifica la conectividad con el servidor central.
     * 
     * @param config La configuracion del departamento que contiene los datos del servidor central
     * @return true si el servidor central responde al ping
     */
    public boolean verificarConectividad(ConfiguracionDepartamento config) {
        try {
            // Crear proxy al servidor central usando ReceptorVotos (interfaz que implementa el servidor central)
            String proxyString = String.format("ReceptorVotos:tcp -h %s -p %d", 
                                             config.getServidorCentralHost(), 
                                             config.getServidorCentralPuerto());
            
            com.zeroc.Ice.ObjectPrx proxy = communicator.stringToProxy(proxyString);
            ReceptorVotosPrx servidorPrx = ReceptorVotosPrx.checkedCast(proxy);
            
            if (servidorPrx == null) {
                return false;
            }
            
            // Hacer ping para verificar conectividad
            return servidorPrx.ping();
            
        } catch (Exception e) {
            return false;
        }
    }
      /**
     * Verifica conectividad con el broker destino especifico (consistente con broker)
     */
    public boolean verificarConectividadDestino(ConfiguracionDepartamento config) {
        return verificarConectividad(config);
    }
      /**
     * Obtiene informacion del servidor central si tiene una interfaz de informacion.
     * Por ahora solo verifica conectividad, pero se puede extender para obtener informacion del servidor.
     * 
     * @param config La configuracion del departamento que contiene los datos del servidor central
     * @return Informacion del servidor o null si no se puede obtener
     */
    public InfoNodo obtenerInfoNodo(ConfiguracionDepartamento config) {
        // Por ahora solo retornamos informacion basica si hay conectividad
        // En el futuro se podria implementar una interfaz Ice para obtener informacion del servidor
        if (verificarConectividad(config)) {
            String servidorInfo = config.getServidorCentralHost() + ":" + config.getServidorCentralPuerto();
            return new InfoNodo("SERVIDOR-CENTRAL", "Servidor Central", "servidor", servidorInfo);
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
     * Clase para almacenar informacion de un nodo (consistente con la del broker)
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
