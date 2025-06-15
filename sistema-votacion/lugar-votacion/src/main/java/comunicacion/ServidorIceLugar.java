package comunicacion;

import VotingSystem.*;
import model.Voto;
import controller.LugarController;

/**
 * Servidor Ice que recibe votos en el lugar de votacion.
 * 
 * Este servidor es responsable de:
 * - Recibir votos desde el broker a traves de Ice
 * - Convertir los votos del formato Ice al formato Java interno
 * - Delegar el procesamiento (reenvio) al controlador del lugar
 * - Responder a solicitudes de ping para verificacion de conectividad
 * 
 * El servidor no realiza validaciones de negocio, solo conversion de datos
 * y delegacion al controlador correspondiente.
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-06-14
 */
public class ServidorIceLugar implements ReceptorVotos {
    
    private LugarController controller;
    
    /**
     * Constructor que inicializa el servidor con el controlador del lugar.
     * 
     * @param controller Controlador que procesara los votos recibidos
     */
    public ServidorIceLugar(LugarController controller) {
        this.controller = controller;
    }
    
    /**
     * Recibe un voto desde el broker y lo procesa.
     * No realiza validaciones, solo conversion y reenvio al controlador.
     * 
     * @param votoIce Voto en formato Ice recibido desde el broker
     * @param current Contexto de la llamada Ice (no utilizado)
     * @return true si el voto fue procesado exitosamente, false en caso contrario
     */    @Override
    public boolean recibirVoto(VotingSystem.Voto votoIce, com.zeroc.Ice.Current current) {
        try {
            // Convertir Ice a Java y procesar (reenviar)
            Voto votoJava = convertirVotoIceAJava(votoIce);
            return controller.procesarVoto(votoJava);
            
        } catch (Exception e) {
            System.err.println("Error procesando voto en lugar de votacion: " + e.getMessage());
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

    @Override
    public int recibirValidacionVotante(String documento, Integer candidatoId, com.zeroc.Ice.Current current) {
        return controller.validarVoto(documento, candidatoId);
    }
    
    /**
     * Convierte un voto del formato Ice al formato Java interno.
     * Mantiene los tipos Integer para los IDs sin conversion adicional.
     * 
     * @param votoIce Voto en formato Ice a convertir
     * @return Voto en formato Java equivalente
     */
    private Voto convertirVotoIceAJava(VotingSystem.Voto votoIce) {
        model.Candidato candidatoJava = new model.Candidato(
            votoIce.candidato.id,    // Integer directo desde Ice
            votoIce.candidato.nombre,
            votoIce.candidato.partidoPolitico
        );
        
        Voto votoJava = new Voto(
            votoIce.id,              // Integer directo desde Ice
            candidatoJava
        );
        
        return votoJava;
    }
}
