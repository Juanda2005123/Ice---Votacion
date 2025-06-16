package comunicacion;

import VotingSystem.*;
import controller.DepartamentoController;

/**
 * Servidor Ice que recibe deltas en el departamento para Map-Reduce Level 2.
 * 
 * Este servidor es responsable de:
 * - Recibir deltas desde lugares de votación a traves de Ice
 * - Delegar el procesamiento Map-Reduce al controlador del departamento
 * - Responder a solicitudes de ping para verificacion de conectividad
 * - NO implementa validaciones (solo consolidación)
 * 
 * @author Sistema de Votacion
 * @version 2.0 - Map-Reduce Level 2
 * @since 2025-06-15
 */
public class ServidorIceDepartamento implements ReceptorVotos {
    
    private DepartamentoController controller;
    
    /**
     * Constructor que inicializa el servidor con el controlador del departamento.
     * 
     * @param controller Controlador que procesara los deltas recibidos
     */
    public ServidorIceDepartamento(DepartamentoController controller) {
        this.controller = controller;
    }

    /**
     * Recibe un delta desde lugares y lo procesa con Map-Reduce departamental.
     * MAP PHASE: Thread Pool consolida deltas departamentales en paralelo.
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
     * Responde a solicitudes de ping para verificar que el departamento esta activo.
     * 
     * @param current Contexto de la llamada Ice (no utilizado)
     * @return Siempre true indicando que el departamento esta operativo
     */
    @Override
    public boolean ping(com.zeroc.Ice.Current current) {
        return true;
    }

    /**
     * Recibe una validación de votante - NO IMPLEMENTADO EN DEPARTAMENTO.
     * El departamento no realiza validaciones locales, solo consolidación de deltas.
     * 
     * @param documento Documento del votante
     * @param candidatoId ID del candidato elegido
     * @param current Contexto de la llamada Ice (no utilizado)
     * @return No retorna, lanza excepcion
     * @throws UnsupportedOperationException Siempre, ya que no se utiliza en departamento
     */
    @Override
    public int recibirValidacionVotante(String documento, int candidatoId, com.zeroc.Ice.Current current) {
        throw new UnsupportedOperationException("La validacion de ciudadanos no se implementa en el departamento. Los deltas se consolidan y reenvian al servidor central.");
    }
}
