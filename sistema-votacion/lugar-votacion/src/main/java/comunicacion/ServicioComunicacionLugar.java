package comunicacion;

import VotingSystem.*;
import model.Voto;
import config.ConfiguracionLugar;

/**
 * Servicio para ENVIAR votos al broker lugar-departamento.
 * Solo reenvia votos, sin validaciones ni estadisticas.
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
    
    /**     * Funcion principal: Reenvia un voto al broker lugar-departamento
     * Sin validaciones - solo reenvio
     */
    public boolean reenviarVoto(Voto voto) {
        return enviarVotoADestino(voto);
    }    /**
     * Reenvia una validacion de votante al broker lugar-departamento.
     * 
     * @param documento Documento del votante
     * @param candidatoId ID del candidato elegido
     * @return Código de validación del broker destino (0-3)
     */
    public int reenviarValidacionVotante(String documento, Integer candidatoId) {
        return enviarValidacionADestino(documento, candidatoId);
    }
    
    /**
     * Envia una validacion de votante al broker destino.
     * 
     * @param documento Documento del votante
     * @param candidatoId ID del candidato elegido
     * @return Código de validación del broker destino (0-3)
     */
    private int enviarValidacionADestino(String documento, Integer candidatoId) {
        try {
            String proxyString = String.format("BrokerService:tcp -h %s -p %d", 
                                             config.getBrokerDestinoHost(), 
                                             config.getBrokerDestinoPuerto());
            
            com.zeroc.Ice.ObjectPrx proxy = communicator.stringToProxy(proxyString);
            BrokerServicePrx brokerPrx = BrokerServicePrx.checkedCast(proxy);
            
            if (brokerPrx == null) {
                return 4; // No se pudo conectar
            }
            
            return brokerPrx.recibirValidacionVotante(documento, candidatoId);
        } catch (Exception e) {
            return 4; // Error de conexión
        }
    }
    
    /**
     * Envia un voto al broker destino - una sola vez
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
     * Cierra la conexion Ice
     */
    public void cerrarConexion() {
        if (communicator != null) {
            communicator.destroy();
        }
    }
}
