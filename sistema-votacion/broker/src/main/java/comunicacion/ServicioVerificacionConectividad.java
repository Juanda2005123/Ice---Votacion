package comunicacion;

import VotingSystem.*;
import config.ConfiguracionBroker;

/** * Servicio para verificar conectividad con destinos y obtener informacion de nodos.
 * Esta clase es responsable de verificar que los destinos esten activos y obtener su identificacion.
 */
public class ServicioVerificacionConectividad {
    
    private com.zeroc.Ice.Communicator communicator;
    
    public ServicioVerificacionConectividad() {
        try {
            this.communicator = com.zeroc.Ice.Util.initialize();
        } catch (Exception e) {
            throw new RuntimeException("Error inicializando communicator para verificacion: " + e.getMessage());
        }
    }
      /**
     * Verifica la conectividad con un destino especifico.
     * 
     * @param destino El destino a verificar
     * @return true si el destino responde al ping
     */
    public boolean verificarConectividad(ConfiguracionBroker.Destino destino) {
        try {
            // Crear proxy al destino usando ReceptorVotos (interfaz que implementan los lugares)
            String proxyString = String.format("ReceptorVotos:tcp -h %s -p %d", 
                                             destino.getHost(), destino.getPuerto());
            
            com.zeroc.Ice.ObjectPrx proxy = communicator.stringToProxy(proxyString);
            ReceptorVotosPrx receptorPrx = ReceptorVotosPrx.checkedCast(proxy);
            
            if (receptorPrx == null) {
                return false;
            }
            
            // Hacer ping para verificar conectividad
            return receptorPrx.ping();
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Verifica conectividad con todos los destinos (metodo principal para multiples destinos)
     */
    public void verificarConectividadDestinos(ConfiguracionBroker config) {
        for (ConfiguracionBroker.Destino destino : config.getTodosLosDestinos()) {
            verificarConectividadDestino(destino);
        }
    }
    
    /**
     * Verifica conectividad con un destino especifico (wrapper del metodo principal)
     */
    public boolean verificarConectividadDestino(ConfiguracionBroker.Destino destino) {
        return verificarConectividad(destino);
    }
      /**
     * Obtiene informacion del nodo remoto si tiene una interfaz de informacion.
     * Por ahora solo verifica conectividad, pero se puede extender para obtener nodo.id y nodo.nombre.
     * 
     * @param destino El destino del cual obtener informacion
     * @return Informacion del nodo o null si no se puede obtener
     */
    public InfoNodo obtenerInfoNodo(ConfiguracionBroker.Destino destino) {
        // Por ahora solo retornamos la informacion que ya tenemos del destino
        // En el futuro se podria implementar una interfaz Ice para obtener nodo.id y nodo.nombre
        if (verificarConectividad(destino)) {
            return new InfoNodo(destino.getId(), "Nodo Activo", destino.getTipo());
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
     * Clase para almacenar informacion de un nodo
     */
    public static class InfoNodo {
        private String id;
        private String nombre;
        private String tipo;
        
        public InfoNodo(String id, String nombre, String tipo) {
            this.id = id;
            this.nombre = nombre;
            this.tipo = tipo;
        }
        
        public String getId() { return id; }
        public String getNombre() { return nombre; }
        public String getTipo() { return tipo; }
    }
}
