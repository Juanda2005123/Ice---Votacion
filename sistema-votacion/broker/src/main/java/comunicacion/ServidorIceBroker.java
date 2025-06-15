package comunicacion;

import VotingSystem.*;
import model.Voto;
import controller.BrokerController;

/**
 * Servidor Ice que recibe votos en el broker.
 * 
 * Este servidor es responsable de:
 * - Recibir votos desde las mesas de votacion a traves de Ice
 * - Convertir los votos del formato Ice al formato Java interno
 * - Delegar el procesamiento (reenvio) al controlador del broker
 * - Responder a solicitudes de ping para verificacion de conectividad
 * 
 * El servidor no realiza validaciones de negocio, solo conversion de datos
 * y delegacion al controlador correspondiente.
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-06-14
 */
public class ServidorIceBroker implements BrokerService {
    
    private BrokerController controller;
    
    /**
     * Constructor que inicializa el servidor con el controlador del broker.
     * 
     * @param controller Controlador que procesara los votos recibidos
     */
    public ServidorIceBroker(BrokerController controller) {
        this.controller = controller;
    }
    
    /**
     * Recibe un voto desde cualquier cliente y lo reenvia.
     * No realiza validaciones, solo conversion y reenvio.
     * 
     * @param votoIce Voto en formato Ice recibido desde el cliente
     * @param current Contexto de la llamada Ice (no utilizado)
     * @return true si el voto fue procesado exitosamente, false en caso contrario
     */    
    @Override
    public boolean recibirVoto(VotingSystem.Voto votoIce, com.zeroc.Ice.Current current) {
        try {
            // Convertir Ice a Java y reenviar
            Voto votoJava = convertirVotoIceAJava(votoIce);
            return controller.procesarVoto(votoJava);
            
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
     * Recibe una validación de votante desde cualquier cliente y la procesa.
     * No realiza validaciones locales, solo delegacion al controlador.
     * 
     * @param documento Documento del votante
     * @param candidatoId ID del candidato elegido
     * @param current Contexto de la llamada Ice (no utilizado)
     * @return Código de validación del controlador (0-3)
     */
    @Override
    public int recibirValidacionVotante(String documento, int candidatoId, com.zeroc.Ice.Current current) {
        try {
            return controller.validarVoto(documento, candidatoId);
        } catch (Exception e) {
            return 3; // Error de procesamiento
        }
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

    @Override
    public String query(String document, com.zeroc.Ice.Current current) {
        try {
            return controller.consultarLugar(document);
        } catch (Exception e) {
            return null;
        }
    }

}
