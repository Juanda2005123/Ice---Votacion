package comunicacion;

import VotingSystem.*;
import controller.ProxyController;

/**
 * Servidor Ice que recibe validaciones de ciudadanos en el proxy.
 * 
 * Este servidor es responsable de:
 * - Recibir validaciones de ciudadanos desde nodos anteriores a traves de Ice
 * - Delegar el procesamiento (reenvio) al controlador del proxy
 * - Responder a solicitudes de ping para verificacion de conectividad
 * 
 * El servidor NO maneja votos, solo validaciones de ciudadanos,
 * actuando como intermediario en la cadena de validacion.
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-06-15
 */
public class ServidorIceProxy implements ReceptorVotos {
    
    private ProxyController controller;
    
    /**
     * Constructor que inicializa el servidor con el controlador del proxy.
     * 
     * @param controller Controlador que procesara las validaciones recibidas
     */
    public ServidorIceProxy(ProxyController controller) {
        this.controller = controller;
    }      /**
     * Recibe un voto - NO IMPLEMENTADO EN PROXY.
     * El proxy solo maneja validaciones de ciudadanos, no votos.
     * 
     * @param votoIce Voto en formato Ice
     * @param current Contexto de la llamada Ice (no utilizado)
     * @return No retorna, lanza excepcion
     * @throws UnsupportedOperationException Siempre, ya que no se utiliza en proxy
     */
    public boolean recibirVoto(VotingSystem.Voto votoIce, com.zeroc.Ice.Current current) {
        throw new UnsupportedOperationException("El procesamiento de votos no se implementa en el proxy. Solo se procesan validaciones de ciudadanos.");
    }
    
    /**
     * Responde a solicitudes de ping para verificar que el proxy esta activo.
     * 
     * @param current Contexto de la llamada Ice (no utilizado)
     * @return Siempre true indicando que el proxy esta operativo
     */
    @Override
    public boolean ping(com.zeroc.Ice.Current current) {
        return true;
    }
    
    /**
     * Recibe una validacion de votante y la reenvia al nodo destino.
     * El proxy actua como intermediario para validaciones de ciudadanos.
     * 
     * @param documento Documento del votante
     * @param candidatoId ID del candidato elegido
     * @param current Contexto de la llamada Ice (no utilizado)
     * @return Codigo de validacion del nodo destino (0-3)
     */
    @Override
    public int recibirValidacionVotante(String documento, int candidatoId, com.zeroc.Ice.Current current) {
        try {
            return controller.validarCiudadano(documento, candidatoId);
        } catch (Exception e) {
            System.err.println("Error procesando validacion en proxy: " + e.getMessage());
            return 3; // Error de procesamiento
        }
    }
    
    /**
     * Recibe un delta - NO IMPLEMENTADO EN PROXY.
     * El proxy solo maneja validaciones de ciudadanos, no deltas.
     * 
     * @param delta Delta en formato Ice
     * @param current Contexto de la llamada Ice (no utilizado)
     * @return No retorna, lanza excepcion
     * @throws UnsupportedOperationException Siempre, ya que no se utiliza en proxy
     */
    @Override
    public boolean recibirDeltaConteo(DeltaConteo delta, com.zeroc.Ice.Current current) {
        throw new UnsupportedOperationException("El procesamiento de deltas no se implementa en el proxy. Solo se procesan validaciones de ciudadanos.");
    }
}
