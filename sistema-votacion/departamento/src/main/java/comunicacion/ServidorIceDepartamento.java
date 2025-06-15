package comunicacion;

import VotingSystem.*;
import model.Voto;
import controller.DepartamentoController;

/**
 * Servidor Ice que recibe votos en el departamento de votacion.
 * 
 * Este servidor es responsable de:
 * - Recibir votos desde el broker lugar-departamento a traves de Ice
 * - Convertir los votos del formato Ice al formato Java interno
 * - Delegar el procesamiento (reenvio) al controlador del departamento
 * - Responder a solicitudes de ping para verificacion de conectividad
 * 
 * El servidor no realiza validaciones de negocio, solo conversion de datos
 * y delegacion al controlador correspondiente.
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-06-15
 */
public class ServidorIceDepartamento implements ReceptorVotos {
    
    private DepartamentoController controller;
    
    /**
     * Constructor que inicializa el servidor con el controlador del departamento.
     * 
     * @param controller Controlador que procesara los votos recibidos
     */
    public ServidorIceDepartamento(DepartamentoController controller) {
        this.controller = controller;
    }
    /**
     * Recibe un voto desde el broker y lo procesa.
     * No realiza validaciones, solo conversion y reenvio al controlador.
     * 
     * @param votoIce Voto en formato Ice recibido desde el broker
     * @param current Contexto de la llamada Ice (no utilizado)
     * @return true si el voto fue procesado exitosamente, false en caso contrario
     */
    @Override
    public boolean recibirVoto(VotingSystem.Voto votoIce, com.zeroc.Ice.Current current) {
        try {
            // Convertir Ice a Java y procesar (reenviar)
            Voto votoJava = convertirVotoIceAJava(votoIce);
            return controller.procesarVoto(votoJava);
            
        } catch (Exception e) {
            System.err.println("Error procesando voto en departamento de votacion: " + e.getMessage());
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
    }    /**
     * Recibe una validación de votante - NO IMPLEMENTADO EN DEPARTAMENTO.
     * El departamento no realiza validaciones locales, solo reenvio de votos al servidor central.
     * 
     * @param documento Documento del votante
     * @param candidatoId ID del candidato elegido
     * @param current Contexto de la llamada Ice (no utilizado)
     * @return No retorna, lanza excepcion
     * @throws UnsupportedOperationException Siempre, ya que no se utiliza en departamento
     */
    @Override
    public int recibirValidacionVotante(String documento, int candidatoId, com.zeroc.Ice.Current current) {
        throw new UnsupportedOperationException("La validacion de ciudadanos no se implementa en el departamento. Los votos se reenvian directamente al servidor central.");
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
