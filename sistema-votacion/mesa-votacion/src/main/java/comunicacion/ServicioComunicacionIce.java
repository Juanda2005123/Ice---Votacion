package comunicacion;

import VotingSystem.*;
import model.Voto;
import model.Votante;

/**
 * Cliente Ice para enviar votos al servidor central.
 */
public class ServicioComunicacionIce {
    
    private com.zeroc.Ice.Communicator communicator;
    private VotingServicePrx votingServicePrx;
    
    public ServicioComunicacionIce() {
        try {
            // Inicializar communicator
            communicator = com.zeroc.Ice.Util.initialize();
            
            // Crear proxy al servidor
            com.zeroc.Ice.ObjectPrx proxy = communicator.stringToProxy(
                "VotingService:tcp -h localhost -p 10000");
            
            votingServicePrx = VotingServicePrx.checkedCast(proxy);
            
            if (votingServicePrx == null) {
                throw new RuntimeException("No se pudo conectar al servidor Ice");
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Error inicializando cliente Ice: " + e.getMessage());
        }
    }
    
    public boolean enviarVotoVotanteConACK(String mesaId, Voto voto, Votante votante) {
        try {
            // Convertir de clases Java a clases Ice
            VotingSystem.Voto votoIce = convertirVotoJavaAIce(voto);
            VotingSystem.Votante votanteIce = convertirVotanteJavaAIce(votante);
            
            // Enviar al servidor
            return votingServicePrx.enviarVotoVotante(mesaId, votoIce, votanteIce);
            
        } catch (Exception e) {
            System.err.println("Error enviando voto/votante: " + e.getMessage());
            return false;
        }
    }
    
    // Métodos de conversión Java ↔ Ice
    private VotingSystem.Voto convertirVotoJavaAIce(Voto votoJava) {
        // Convertir Candidato
        VotingSystem.Candidato candidatoIce = new VotingSystem.Candidato();
        candidatoIce.cedula = votoJava.getCandidato().getCedula();
        candidatoIce.nombre = votoJava.getCandidato().getNombre();
        candidatoIce.apellido = votoJava.getCandidato().getApellido();
        candidatoIce.partidoPolitico = votoJava.getCandidato().getPartidoPolitico();
        
        // Crear Voto Ice
        VotingSystem.Voto votoIce = new VotingSystem.Voto();
        votoIce.candidato = candidatoIce;
        votoIce.timestamp = java.time.LocalDateTime.now().toString(); // O el timestamp del voto
        
        return votoIce;
    }

    private VotingSystem.Votante convertirVotanteJavaAIce(Votante votanteJava) {
        VotingSystem.Votante votanteIce = new VotingSystem.Votante();
        votanteIce.cedula = votanteJava.getCedula();
        votanteIce.nombre = votanteJava.getNombre();
        votanteIce.apellido = votanteJava.getApellido();
        votanteIce.departamento = votanteJava.getDepartamento();
        votanteIce.ciudad = votanteJava.getCiudad();
        votanteIce.yaVoto = votanteJava.isYaVoto();
        
        return votanteIce;
    }
    
   
    
    public void cerrarConexion() {
        if (communicator != null) {
            communicator.destroy();
        }
    }
}