package comunicacion;

import VotingSystem.*;
import controller.BrokerController;

/**
 * Servidor Ice que recibe deltas en el broker para Map-Reduce.
 * 
 * Este servidor es responsable de:
 * - Recibir deltas desde los nodos inferiores a traves de Ice
 * - Delegar el procesamiento (reenvio) al controlador del broker con Thread Pool
 * - Responder a solicitudes de ping para verificacion de conectividad
 * - Manejar validaciones de ciudadanos
 * 
 * @author Sistema de Votacion
 * @version 2.0 - Map-Reduce Delta System
 * @since 2025-06-15
 */
public class ServidorIceBroker implements BrokerService {
    
    private BrokerController controller;
    
    /**
     * Constructor que inicializa el servidor con el controlador del broker.
     * 
     * @param controller Controlador que procesara los deltas recibidos
     */
    public ServidorIceBroker(BrokerController controller) {
        this.controller = controller;
    }
    
    /**
     * Recibe un delta desde nodos inferiores y lo procesa con Thread Pool.
     * 
     * @param delta Delta en formato Ice recibido desde el cliente
     * @param current Contexto de la llamada Ice (no utilizado)
     * @return true si el delta fue procesado exitosamente, false en caso contrario
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
     * Responde a solicitudes de ping para verificar que el broker esta activo.
     * 
     * @param current Contexto de la llamada Ice (no utilizado)
     * @return Siempre true indicando que el broker esta operativo
     */    
    @Override
    public boolean ping(com.zeroc.Ice.Current current) {
        return true;
    }

    /**
     * Recibe una validación de ciudadano desde cualquier cliente y la procesa.
     * No realiza validaciones locales, solo delegacion al controlador.
     * 
     * @param documento Documento del ciudadano
     * @param candidatoId ID del candidato elegido
     * @param current Contexto de la llamada Ice (no utilizado)
     * @return Código de validación del controlador: 1=existe, 3=no existe, 4=error
     */
    @Override
    public int recibirValidacionVotante(String documento, int candidatoId, com.zeroc.Ice.Current current) {
        try {
            return controller.validarCiudadano(documento, candidatoId);
        } catch (Exception e) {
            return 4; // Error de procesamiento
        }
    }
}