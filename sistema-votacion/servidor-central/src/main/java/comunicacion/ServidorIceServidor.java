package comunicacion;

import VotingSystem.*;
import controller.ServidorController;

/**
 * Servidor Ice que recibe deltas en el servidor central para Reduce Final.
 * 
 * Este servidor es responsable de:
 * - Recibir deltas departamentales a través de Ice
 * - Delegar el procesamiento Reduce Final al controlador del servidor central
 * - Responder a solicitudes de ping para verificación de conectividad
 * 
 * El servidor es el destino final del flujo Map-Reduce, solo recibe y consolida,
 * no reenvía deltas a ningún otro destino.
 * 
 * @author Sistema de Votacion
 * @version 3.0 - Reduce Final
 * @since 2025-06-15
 */
public class ServidorIceServidor implements ReceptorVotos {
    
    private ServidorController controller;
    
    /**
     * Constructor que inicializa el servidor con el controlador del servidor central.
     * 
     * @param controller Controlador que procesará los deltas recibidos
     */
    public ServidorIceServidor(ServidorController controller) {
        this.controller = controller;
    }    /**
     * Recibe un delta desde departamentos y lo procesa con Reduce Final.
     * El servidor central es el destino final de la consolidación Map-Reduce.
     * 
     * @param delta Delta departamental en formato Ice
     * @param current Contexto de la llamada Ice (no utilizado)
     * @return true si el delta fue procesado exitosamente, false en caso contrario
     */
    @Override
    public boolean recibirDeltaConteo(DeltaConteo delta, com.zeroc.Ice.Current current) {
        try {
            // Procesar delta con Reduce Final
            return controller.procesarDelta(delta);
            
        } catch (Exception e) {
            System.err.println("Error procesando delta en servidor central: " + e.getMessage());
            return false;
        }    }
      /**
     * Responde a solicitudes de ping para verificar que el servidor central está activo.
     * 
     * @param current Contexto de la llamada Ice (no utilizado)
     * @return Siempre true indicando que el servidor central está operativo
     */
    @Override
    public boolean ping(com.zeroc.Ice.Current current) {
        return true;
    }
      /**
     * Recibe una validación de votante - NO IMPLEMENTADO EN SERVIDOR CENTRAL.
     * El servidor central es el destino final, solo recibe deltas procesados.
     * 
     * @param documento Documento del votante
     * @param candidatoId ID del candidato elegido
     * @param current Contexto de la llamada Ice (no utilizado)
     * @return No retorna, lanza excepcion
     * @throws UnsupportedOperationException Siempre, ya que no se utiliza en servidor central
     */
    @Override
    public int recibirValidacionVotante(String documento, int candidatoId, com.zeroc.Ice.Current current) {
        throw new UnsupportedOperationException("La validacion de votantes no se implementa en el servidor central. Solo se reciben deltas consolidados.");
    }
}
