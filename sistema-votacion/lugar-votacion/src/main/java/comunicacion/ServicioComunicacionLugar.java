package comunicacion;

import VotingSystem.*;
import model.Voto;
import config.ConfiguracionLugar;

/**
 * Servicio para ENVIAR votos al broker lugar-departamento.
 * SIMPLE: solo reenvía votos, sin validaciones ni estadísticas.
 * (Consistente con ServicioComunicacionBroker)
 */
public class ServicioComunicacionLugar {
    
    private com.zeroc.Ice.Communicator communicator;
    private ConfiguracionLugar config;
    
    public ServicioComunicacionLugar(ConfiguracionLugar config) {
        this.config = config;
        
        try {
            communicator = com.zeroc.Ice.Util.initialize();
        } catch (Exception e) {
            throw new RuntimeException("Error inicializando cliente Ice: " + e.getMessage());
        }
    }
    
    /**
     * FUNCIÓN PRINCIPAL: Reenvía un voto al broker lugar-departamento
     * SIN validaciones - SOLO reenvío
     */
    public boolean reenviarVoto(Voto voto) {
        return enviarVotoADestino(voto);
    }
    
    /**
     * Envía un voto al broker destino - UNA SOLA VEZ
     */
    private boolean enviarVotoADestino(Voto voto) {
        try {
            String proxyString = String.format("BrokerService:tcp -h %s -p %d", 
                                             config.getBrokerDestinoHost(), 
                                             config.getBrokerDestinoPuerto());
            
            com.zeroc.Ice.ObjectPrx proxy = communicator.stringToProxy(proxyString);
            BrokerServicePrx brokerPrx = BrokerServicePrx.checkedCast(proxy);
            
            if (brokerPrx == null) {
                return false;
            }
            
            VotingSystem.Voto votoIce = convertirVotoJavaAIce(voto);
            return brokerPrx.recibirVoto(votoIce);
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Convertir Voto Java a Ice - MANTIENE Integer IDs
     */
    private VotingSystem.Voto convertirVotoJavaAIce(Voto votoJava) {
        VotingSystem.Candidato candidatoIce = new VotingSystem.Candidato();
        candidatoIce.id = votoJava.getCandidato().getId(); // Integer directo
        candidatoIce.nombre = votoJava.getCandidato().getNombre();
        candidatoIce.partidoPolitico = votoJava.getCandidato().getPartidoPolitico();
        
        VotingSystem.Voto votoIce = new VotingSystem.Voto();
        votoIce.id = votoJava.getId(); // Integer directo
        votoIce.candidato = candidatoIce;
        
        return votoIce;
    }
    
    /**
     * Cierra la conexión Ice
     */
    public void cerrarConexion() {
        if (communicator != null) {
            communicator.destroy();
        }
    }
}
