package comunicacion;

import VotingSystem.*;
import controller.LugarController;

/**
 * Servidor Ice que recibe deltas en el lugar de votación para Map-Reduce Level 1.
 * 
 * Este servidor es responsable de:
 * - Recibir deltas desde brokers/mesas a traves de Ice
 * - Delegar el procesamiento Map-Reduce al controlador del lugar
 * - Responder a solicitudes de ping para verificacion de conectividad
 * - Manejar validaciones de ciudadanos
 * 
 * @author Sistema de Votacion
 * @version 2.0 - Map-Reduce Level 1
 * @since 2025-06-15
 */
public class ServidorIceLugar implements ReceptorVotos {
    
    private LugarController controller;
    
    /**
     * Constructor que inicializa el servidor con el controlador del lugar.
     * 
     * @param controller Controlador que procesara los deltas recibidos
     */
    public ServidorIceLugar(LugarController controller) {
        this.controller = controller;
    }
    
    /**
     * Recibe un delta desde brokers/mesas y lo procesa con Map-Reduce.
     * MAP PHASE: Thread Pool consolida deltas en paralelo.
     * 
     * @param delta Delta en formato Ice recibido desde el cliente
     * @param current Contexto de la llamada Ice (no utilizado)
     * @return true si el delta fue procesado exitosamente
     */    
    @Override
    public boolean recibirDeltaConteo(VotingSystem.DeltaConteo delta, com.zeroc.Ice.Current current) {
        try {
            return controller.procesarDelta(delta);
        } catch (Exception e) {
            return false;
        }
    }    
    /**
     * Responde a solicitudes de ping para verificar que el lugar esta activo.
     * 
     * @param current Contexto de la llamada Ice (no utilizado)
     * @return Siempre true indicando que el lugar esta operativo
     */
    @Override
    public boolean ping(com.zeroc.Ice.Current current) {
        return true;
    }

    /**
     * Recibe una validación de votante desde el broker y la reenvia.
     * No realiza validaciones locales, solo reenvio al siguiente broker.
     * 
     * @param documento Documento del votante
     * @param candidatoId ID del candidato elegido
     * @param current Contexto de la llamada Ice (no utilizado)
     * @return Código de validación del broker destino (0-3)
     */
    @Override
    public int recibirValidacionVotante(String documento, int candidatoId, com.zeroc.Ice.Current current) {
        try {
            return controller.validarVoto(documento, candidatoId);
        } catch (Exception e) {
            return 4; // Error de procesamiento
        }
    }
}
