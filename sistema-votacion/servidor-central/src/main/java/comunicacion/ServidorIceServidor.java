package comunicacion;

import VotingSystem.*;
import model.Voto;
import controller.ServidorController;

/**
 * Servidor Ice que recibe votos en el servidor central de votacion.
 * 
 * Este servidor es responsable de:
 * - Recibir votos desde el broker departamento-central a traves de Ice
 * - Convertir los votos del formato Ice al formato Java interno
 * - Delegar el procesamiento (impresion) al controlador del servidor central
 * - Responder a solicitudes de ping para verificacion de conectividad
 * 
 * El servidor es el destino final del flujo de votos, solo recibe e imprime,
 * no reenvia votos a ningun otro destino.
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-06-15
 */
public class ServidorIceServidor implements ReceptorVotos {
    
    private ServidorController controller;
    
    /**
     * Constructor que inicializa el servidor con el controlador del servidor central.
     * 
     * @param controller Controlador que procesara los votos recibidos
     */
    public ServidorIceServidor(ServidorController controller) {
        this.controller = controller;
    }    /**
     * Recibe un voto desde el broker y lo procesa.
     * El servidor central es el destino final, solo recibe e imprime votos.
     * 
     * @param votoIce Voto en formato Ice recibido desde el broker
     * @param current Contexto de la llamada Ice (no utilizado)
     * @return true si el voto fue procesado exitosamente, false en caso contrario
     */
    @Override
    public boolean recibirVoto(VotingSystem.Voto votoIce, com.zeroc.Ice.Current current) {
        try {
            // Convertir Ice a Java y procesar (imprimir)
            Voto votoJava = convertirVotoIceAJava(votoIce);
            return controller.procesarVoto(votoJava);
            
        } catch (Exception e) {
            System.err.println("Error procesando voto en servidor central: " + e.getMessage());
            return false;
        }
    }
      /**
     * Responde a solicitudes de ping para verificar que el servidor central esta activo.
     * 
     * @param current Contexto de la llamada Ice (no utilizado)
     * @return Siempre true indicando que el servidor central esta operativo
     */
    @Override
    public boolean ping(com.zeroc.Ice.Current current) {
        return true;
    }
    
    /**
     * Recibe una validacion de votante - NO IMPLEMENTADO EN SERVIDOR CENTRAL.
     * El servidor central es el destino final, solo recibe votos procesados.
     * 
     * @param documento Documento del votante
     * @param candidatoId ID del candidato elegido
     * @param current Contexto de la llamada Ice (no utilizado)
     * @return No retorna, lanza excepcion
     * @throws UnsupportedOperationException Siempre, ya que no se utiliza en servidor central
     */
    @Override
    public int recibirValidacionVotante(String documento, int candidatoId, com.zeroc.Ice.Current current) {
        throw new UnsupportedOperationException("La validacion de votantes no se implementa en el servidor central. Solo se reciben votos ya procesados.");
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
