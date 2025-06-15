package comunicacion;

import VotingSystem.*;
import model.Voto;
import config.ConfiguracionLugar;

/**
 * Servicio para ENVIAR votos al broker lugar-departamento.
 * FUNCIÓN: Reenvía votos desde el lugar de votación al siguiente broker
 */
public class ServicioComunicacionLugar {
    
    private com.zeroc.Ice.Communicator communicator;
    private BrokerServicePrx brokerProxy;
    private ConfiguracionLugar config;
    
    public ServicioComunicacionLugar(ConfiguracionLugar config) {
        this.config = config;
        
        try {
            // Inicializar Ice communicator
            communicator = com.zeroc.Ice.Util.initialize();
            
            // Crear proxy al broker lugar-departamento
            String proxyString = String.format("BrokerService:tcp -h %s -p %d", 
                                             config.getBrokerDestinoHost(), 
                                             config.getBrokerDestinoPuerto());
            
            com.zeroc.Ice.ObjectPrx proxy = communicator.stringToProxy(proxyString);
            brokerProxy = BrokerServicePrx.checkedCast(proxy);
            
            if (brokerProxy == null) {
                throw new RuntimeException("No se pudo conectar al broker lugar-departamento en " + 
                                         config.getBrokerDestinoHost() + ":" + 
                                         config.getBrokerDestinoPuerto());
            }
            
            System.out.println("ServicioComunicacionLugar: Conectado al broker lugar-departamento " + 
                             config.getBrokerDestinoHost() + ":" + config.getBrokerDestinoPuerto());
            
        } catch (Exception e) {
            System.err.println("Error inicializando conexión al broker lugar-departamento: " + e.getMessage());
            throw new RuntimeException("No se pudo inicializar conexión al broker lugar-departamento");
        }
            
    }
    
    /**
     * FUNCIÓN PRINCIPAL: Reenvía un voto al broker lugar-departamento
     */
    public boolean reenviarVoto(Voto voto) {
        try {
            // Convertir voto Java a Ice
            VotingSystem.Voto votoIce = convertirVotoJavaAIce(voto);
            
            // Enviar al broker lugar-departamento
            return brokerProxy.recibirVoto(votoIce);
            
        } catch (Exception e) {
            System.err.println("Error reenviando voto: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Verifica conectividad con el broker lugar-departamento
     */
    public boolean verificarConectividad() {
        try {
            return brokerProxy != null && brokerProxy.ping();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Convertir Voto Java a Ice - MANTIENE Integer IDs
     */
    private VotingSystem.Voto convertirVotoJavaAIce(Voto votoJava) {
        VotingSystem.Candidato candidatoIce = new VotingSystem.Candidato();
        candidatoIce.id = votoJava.getCandidato().getId();
        candidatoIce.nombre = votoJava.getCandidato().getNombre();
        candidatoIce.partidoPolitico = votoJava.getCandidato().getPartidoPolitico();
        
        VotingSystem.Voto votoIce = new VotingSystem.Voto();
        votoIce.id = votoJava.getId();
        votoIce.candidato = candidatoIce;
        
        return votoIce;
    }
    
    /**
     * Cierra la conexión Ice
     */
    public void cerrarConexion() {
        if (communicator != null) {
            communicator.destroy();
            System.out.println("Conexión al broker lugar-departamento cerrada");
        }
    }
}
