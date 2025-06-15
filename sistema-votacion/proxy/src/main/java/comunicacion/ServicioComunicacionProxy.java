package comunicacion;

import VotingSystem.*;
import config.ConfiguracionProxy;

/**
 * Servicio para REENVIAR validaciones de ciudadanos al nodo destino.
 * Solo maneja validaciones, NO votos.
 */
public class ServicioComunicacionProxy {
    
    private com.zeroc.Ice.Communicator communicator;
    private ConfiguracionProxy config;
    
    public ServicioComunicacionProxy(ConfiguracionProxy config) {
        this.config = config;
        
        try {
            communicator = com.zeroc.Ice.Util.initialize();
        } catch (Exception e) {
            throw new RuntimeException("Error inicializando cliente Ice: " + e.getMessage());
        }
    }    
    /**
     * Reenvia un voto - NO IMPLEMENTADO EN PROXY.
     * El proxy solo maneja validaciones de ciudadanos, no votos.
     * 
     * @param voto Voto a reenviar
     * @return No retorna, lanza excepcion
     * @throws UnsupportedOperationException Siempre, ya que no se utiliza en proxy
     */
    public boolean reenviarVoto(model.Voto voto) {
        throw new UnsupportedOperationException("El reenvio de votos no se implementa en el proxy. Solo se procesan validaciones de ciudadanos.");
    }
    
    /**
     * Reenvia una validacion de votante al nodo destino.
     * 
     * @param documento Documento del votante
     * @param candidatoId ID del candidato elegido
     * @return Codigo de validacion del nodo destino (0-3)
     */
    public int reenviarValidacionVotante(String documento, Integer candidatoId) {
        return enviarValidacionADestino(documento, candidatoId);
    }    
    /**
     * Envia una validacion de votante al nodo destino.
     * 
     * @param documento Documento del votante
     * @param candidatoId ID del candidato elegido
     * @return Codigo de validacion del nodo destino (0-3)
     */
    private int enviarValidacionADestino(String documento, Integer candidatoId) {
        try {
            String proxyString = String.format("ReceptorVotos:tcp -h %s -p %d", 
                                             config.getNodoDestinoHost(), 
                                             config.getNodoDestinoPuerto());
            
            com.zeroc.Ice.ObjectPrx proxy = communicator.stringToProxy(proxyString);
            ReceptorVotosPrx receptorPrx = ReceptorVotosPrx.checkedCast(proxy);
            
            if (receptorPrx == null) {
                System.err.println("[ERROR] No se pudo conectar con nodo destino");
                return 3; // No se pudo conectar
            }
            
            return receptorPrx.recibirValidacionVotante(documento, candidatoId);
        } catch (Exception e) {
            System.err.println("[ERROR] Error enviando validacion al nodo destino: " + e.getMessage());
            return 3; // Error de conexion
        }
    }
    
    /**
     * Cierra la conexion Ice
     */
    public void cerrarConexion() {
        if (communicator != null) {
            communicator.destroy();
        }
    }
}
