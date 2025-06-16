package comunicacion;

import VotingSystem.*;
import config.ConfiguracionDepartamento;

/**
 * Servicio para enviar deltas consolidadas departamentales al servidor central.
 * Solo reenvia deltas Map-Reduce Level 2, sin validaciones ni estadisticas.
 * 
 * @author Sistema de Votacion
 * @version 2.0 - Map-Reduce Level 2 
 * @since 2025-06-15
 */
public class ServicioComunicacionDepartamento {
    
    private com.zeroc.Ice.Communicator communicator;
    private ConfiguracionDepartamento config;
    
    public ServicioComunicacionDepartamento(ConfiguracionDepartamento config) {
        this.config = config;
        
        try {
            communicator = com.zeroc.Ice.Util.initialize();
        } catch (Exception e) {
            throw new RuntimeException("Error inicializando cliente Ice: " + e.getMessage());
        }
    }

    /**
     * Reenvia un delta consolidado departamental al servidor central.
     * REDUCE PHASE: Envía resultado de consolidación Map-Reduce Level 2.
     * 
     * @param delta Delta consolidado departamental a reenviar
     * @return true si el delta fue reenviado exitosamente
     */
    public boolean reenviarDelta(DeltaConteo delta) {
        return enviarDeltaAServidor(delta);
    }      /**
     * Recibe una validacion de votante - NO IMPLEMENTADO EN DEPARTAMENTO.
     * El departamento no realiza validaciones, solo consolidación de deltas.
     * 
     * @param documento Documento del votante
     * @param candidatoId ID del candidato elegido
     * @return No retorna, lanza excepcion
     * @throws UnsupportedOperationException Siempre, ya que no se utiliza en departamento
     */
    public int reenviarValidacionVotante(String documento, Integer candidatoId) {
        throw new UnsupportedOperationException("La validacion de ciudadanos no se implementa en el departamento. Los deltas se consolidan y reenvian al servidor central.");
    }
    
    /**
     * Envia un delta consolidado departamental al servidor central directamente.
     * 
     * @param delta Delta consolidado departamental a enviar
     * @return true si el delta fue enviado exitosamente, false en caso contrario
     */
    private boolean enviarDeltaAServidor(DeltaConteo delta) {
        try {
            String proxyString = String.format("ReceptorVotos:tcp -h %s -p %d", 
                                             config.getServidorCentralHost(), 
                                             config.getServidorCentralPuerto());
            
            com.zeroc.Ice.ObjectPrx proxy = communicator.stringToProxy(proxyString);
            ReceptorVotosPrx receptorPrx = ReceptorVotosPrx.checkedCast(proxy);
            
            if (receptorPrx == null) {
                return false;
            }
            
            return receptorPrx.recibirDeltaConteo(delta);
            
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
