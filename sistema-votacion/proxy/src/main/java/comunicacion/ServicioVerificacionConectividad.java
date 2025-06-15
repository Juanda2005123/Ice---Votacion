package comunicacion;

import VotingSystem.*;
import config.ConfiguracionProxy;

/**
 * Servicio para verificar conectividad con el nodo destino del proxy.
 * Esta clase es responsable de verificar que el nodo destino este activo.
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
     * Verifica la conectividad con el nodo destino configurado en el proxy.
     * 
     * @param config Configuracion del proxy con el nodo destino
     * @return true si el nodo destino responde al ping
     */
    public boolean verificarConectividad(ConfiguracionProxy config) {
        try {
            // Crear proxy al nodo destino usando ReceptorVotos
            String proxyString = String.format("ReceptorVotos:tcp -h %s -p %d", 
                                             config.getNodoDestinoHost(), 
                                             config.getNodoDestinoPuerto());
            
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
     * Verifica conectividad con el nodo destino e imprime resultado.
     * 
     * @param config Configuracion del proxy
     */
    public void verificarConectividadDestino(ConfiguracionProxy config) {
        boolean conectado = verificarConectividad(config);
        
        if (conectado) {
            System.out.println("[OK] Nodo destino " + config.getNodoDestinoHost() + 
                             ":" + config.getNodoDestinoPuerto() + " esta activo");
        } else {
            System.out.println("[ERROR] Nodo destino " + config.getNodoDestinoHost() + 
                             ":" + config.getNodoDestinoPuerto() + " no responde");
        }
    }    
    /**
     * Cierra el communicator
     */
    public void cerrar() {
        if (communicator != null) {
            communicator.destroy();
        }
    }
}
