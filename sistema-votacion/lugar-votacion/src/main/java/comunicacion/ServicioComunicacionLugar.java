package comunicacion;

import VotingSystem.*;
import config.ConfiguracionLugar;

/**
 * Servicio para enviar deltas consolidadas al broker lugar-departamento.
 * Solo reenvia deltas Map-Reduce, sin validaciones ni estadisticas.
 * 
 * @author Sistema de Votacion
 * @version 2.0 - Map-Reduce Level 1 
 * @since 2025-06-15
 */
public class ServicioComunicacionLugar {
    
    private com.zeroc.Ice.Communicator communicator;
    private ConfiguracionLugar config;
    
    public ServicioComunicacionLugar(ConfiguracionLugar config) {
        this.config = config;
        
        try {
            communicator = com.zeroc.Ice.Util.initialize();
        } catch (Exception e) {
            throw new RuntimeException("Error inicializando cliente Ice: " + e.getMessage());
        }
    }
    
    /**
     * Reenvia un delta consolidado al broker lugar-departamento.
     * REDUCE PHASE: Envía resultado de consolidación Map-Reduce.
     * 
     * @param delta Delta consolidado a reenviar
     * @return true si el delta fue reenviado exitosamente
     */
    public boolean reenviarDelta(DeltaConteo delta) {
        return enviarDeltaADestino(delta);
    }    /**
     * Reenvia una validacion de votante al broker lugar-departamento.
     * 
     * @param documento Documento del votante
     * @param candidatoId ID del candidato elegido
     * @return Código de validación del broker destino (0-3)
     */
    public int reenviarValidacionVotante(String documento, Integer candidatoId) {
        return enviarValidacionADestino(documento, candidatoId);
    }
    
    /**
     * Envia una validacion de votante al broker destino.
     * 
     * @param documento Documento del votante
     * @param candidatoId ID del candidato elegido
     * @return Código de validación del broker destino (0-3)
     */
    private int enviarValidacionADestino(String documento, Integer candidatoId) {
        try {
            String proxyString = String.format("BrokerService:tcp -h %s -p %d", 
                                             config.getBrokerDestinoHost(), 
                                             config.getBrokerDestinoPuerto());
            
            com.zeroc.Ice.ObjectPrx proxy = communicator.stringToProxy(proxyString);
            BrokerServicePrx brokerPrx = BrokerServicePrx.checkedCast(proxy);
            
            if (brokerPrx == null) {
                return 4; // No se pudo conectar
            }
            
            return brokerPrx.recibirValidacionVotante(documento, candidatoId);
        } catch (Exception e) {
            return 4; // Error de conexión
        }
    }
    
    /**
     * Envia un delta consolidado al broker destino usando Map-Reduce.
     * 
     * @param delta Delta consolidado a enviar
     * @return true si el delta fue enviado exitosamente
     */
    private boolean enviarDeltaADestino(DeltaConteo delta) {
        try {
            String proxyString = String.format("BrokerService:tcp -h %s -p %d", 
                                             config.getBrokerDestinoHost(), 
                                             config.getBrokerDestinoPuerto());
            
            com.zeroc.Ice.ObjectPrx proxy = communicator.stringToProxy(proxyString);
            BrokerServicePrx brokerPrx = BrokerServicePrx.checkedCast(proxy);
            
            if (brokerPrx == null) {
                return false;
            }
            
            return brokerPrx.recibirDeltaConteo(delta);
            
        } catch (Exception e) {
            return false;
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
