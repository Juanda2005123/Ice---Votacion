package comunicacion;

import VotingSystem.*;
import model.Voto;
import controller.LugarController;

/**
 * Servidor Ice que RECIBE votos en el lugar de votación.
 * FUNCIÓN: Recibe votos del broker mesa-lugar y los pasa al controlador
 */
public class ServidorIceLugar implements ReceptorVotos {
    
    private LugarController controller;
    
    public ServidorIceLugar(LugarController controller) {
        this.controller = controller;
    }
    
    /**
     * RECIBE un voto desde el broker mesa-lugar y lo procesa
     * SIN validaciones - SOLO conversión y reenvío al controlador
     */
    @Override
    public boolean recibirVoto(VotingSystem.Voto votoIce, com.zeroc.Ice.Current current) {
        try {
            // Convertir Ice a Java y procesar (reenviar)
            Voto votoJava = convertirVotoIceAJava(votoIce);
            return controller.procesarVoto(votoJava);
            
        } catch (Exception e) {
            System.err.println("Error procesando voto en lugar de votación: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Ping para verificar que el lugar de votación está activo
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
