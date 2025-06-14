package comunicacion;

import VotingSystem.*;
import model.Voto;
import controller.BrokerController;

/**
 * Servidor Ice que RECIBE votos en el broker.
 * SIMPLE: recibe el voto, lo convierte y lo pasa al controller para reenvío.
 */
public class ServidorIceBroker implements BrokerService {
    
    private BrokerController controller;
    
    public ServidorIceBroker(BrokerController controller) {
        this.controller = controller;
    }
      /**
     * RECIBE un voto desde cualquier cliente y lo reenvía
     * SIN validaciones - SOLO conversión y reenvío
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
     * Ping para verificar que el broker está activo
     */
    @Override
    public boolean ping(com.zeroc.Ice.Current current) {
        return true;
    }
    
    /**
     * Convertir Voto Ice a Java - MANTIENE Integer IDs
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
