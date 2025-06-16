package comunicacion;

import VotingSystem.*;
import config.ConfiguracionBroker;
import enrutamiento.EstrategiaEnrutamiento;

/**
 * Servicio para reenviar deltas a destinos configurados con load balancing.
 * Solo reenvia deltas usando Map-Reduce, sin validaciones ni estadisticas.
 * 
 * @author Sistema de Votacion
 * @version 2.0 - Delta System con Thread Pool
 * @since 2025-06-15
 */
public class ServicioComunicacionBroker {
    
    private com.zeroc.Ice.Communicator communicator;
    private EstrategiaEnrutamiento estrategiaEnrutamiento;
    private ConfiguracionBroker config;

    public ServicioComunicacionBroker(ConfiguracionBroker config) {
        this.config = config;
        this.estrategiaEnrutamiento = new EstrategiaEnrutamiento(config);
        
        try {
            communicator = com.zeroc.Ice.Util.initialize();
        } catch (Exception e) {
            throw new RuntimeException("Error inicializando cliente Ice: " + e.getMessage());
        }
    }

    /**
     * Reenvia un delta al destino seleccionado usando load balancing LRU.
     * Sin validaciones - solo reenvio con Thread Pool.
     */    
    public boolean reenviarDelta(DeltaConteo delta) {
        ConfiguracionBroker.Destino destino = seleccionarDestino();
        
        if (destino == null) {
            return false;
        }
        
        return enviarDeltaADestino(delta, destino);
    }      /**
     * Reenvia una validacion de votante al destino seleccionado.
     * 
     * @param documento Documento del votante
     * @param candidatoId ID del candidato elegido
     * @return Código de validación del destino (0-3)
     */
    public int reenviarValidacionVotante(String documento, Integer candidatoId) {
        ConfiguracionBroker.Destino destino = seleccionarDestino();
        
        if (destino == null) {
            return 3; // No existe destino disponible
        }
        
        return enviarValidacionADestino(documento, candidatoId, destino);
    }
      /**
     * Envia una validacion de ciudadano al proxy de validacion configurado.
     * Este método se conecta directamente al proxy para validar ciudadanos.
     * 
     * @param documento Documento del ciudadano a validar
     * @param candidatoId ID del candidato elegido (no utilizado en validacion)
     * @return Código de validación del proxy: 1=existe, 3=no existe, 4=error
     */
    public int enviarValidacionAProxy(String documento, Integer candidatoId) {
        try {
            String proxyString = String.format("ReceptorVotos:tcp -h %s -p %d", 
                                             config.getProxyHost(), 
                                             config.getProxyPuerto());
            
            com.zeroc.Ice.ObjectPrx proxy = communicator.stringToProxy(proxyString);
            ReceptorVotosPrx receptorPrx = ReceptorVotosPrx.checkedCast(proxy);
            
            if (receptorPrx == null) {
                return 4; // Error de conexión
            }
            
            return receptorPrx.recibirValidacionVotante(documento, candidatoId);
        } catch (Exception e) {
            return 4; // Error de procesamiento
        }
    }

    private ConfiguracionBroker.Destino seleccionarDestino() {
        return estrategiaEnrutamiento.seleccionarDestino();
    }    /**
     * Envia una validacion de votante a un destino especifico.
     * 
     * @param documento Documento del votante
     * @param candidatoId ID del candidato elegido
     * @param destino Destino al cual enviar la validacion
     * @return Código de validación del destino (0-3)
     */
    private int enviarValidacionADestino(String documento, Integer candidatoId, ConfiguracionBroker.Destino destino) {
        try {
            String proxyString = String.format("ReceptorVotos:tcp -h %s -p %d", 
                                             destino.getHost(), destino.getPuerto());
            
            com.zeroc.Ice.ObjectPrx proxy = communicator.stringToProxy(proxyString);
            ReceptorVotosPrx receptorPrx = ReceptorVotosPrx.checkedCast(proxy);
            
            if (receptorPrx == null) {
                return 3; // No se pudo conectar
            }
            
            return receptorPrx.recibirValidacionVotante(documento, candidatoId);
        } catch (Exception e) {
            return 3; // Error de conexión
        }
    }

    /**
     * Envia un delta a un destino especifico usando Map-Reduce.
     */
    private boolean enviarDeltaADestino(DeltaConteo delta, ConfiguracionBroker.Destino destino) {
        try {
            String proxyString = String.format("ReceptorVotos:tcp -h %s -p %d", 
                                             destino.getHost(), destino.getPuerto());
            
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