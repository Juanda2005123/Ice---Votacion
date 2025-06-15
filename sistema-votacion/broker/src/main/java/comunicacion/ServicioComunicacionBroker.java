package comunicacion;

import VotingSystem.*;
import model.Voto;
import config.ConfiguracionBroker;
import enrutamiento.EstrategiaEnrutamiento;

/**
 * Servicio para ENVIAR votos a destinos configurados.
 * SIMPLE: solo reenvía votos, sin validaciones ni estadísticas.
 */
public class ServicioComunicacionBroker {
    
    private com.zeroc.Ice.Communicator communicator;
    private EstrategiaEnrutamiento estrategiaEnrutamiento;
      
    public ServicioComunicacionBroker(ConfiguracionBroker config) {
        this.estrategiaEnrutamiento = new EstrategiaEnrutamiento(config);
        
        try {
            communicator = com.zeroc.Ice.Util.initialize();
        } catch (Exception e) {
            throw new RuntimeException("Error inicializando cliente Ice: " + e.getMessage());
        }
    }
      /**
     * FUNCIÓN PRINCIPAL: Reenvía un voto al destino seleccionado
     * SIN validaciones - SOLO reenvío
     * DIFERENCIA CON LUGAR: Verifica conectividad antes de enviar
     */    
    public boolean reenviarVoto(Voto voto) {
        ConfiguracionBroker.Destino destino = estrategiaEnrutamiento.seleccionarDestino();
        
        if (destino == null) {
            return false;
        }
        
        return enviarVotoADestino(voto, destino);
    }
      /**
     * Envía un voto a un destino específico - UNA SOLA VEZ
     */
    private boolean enviarVotoADestino(Voto voto, ConfiguracionBroker.Destino destino) {
        try {
            String proxyString = String.format("ReceptorVotos:tcp -h %s -p %d", 
                                             destino.getHost(), destino.getPuerto());
            
            com.zeroc.Ice.ObjectPrx proxy = communicator.stringToProxy(proxyString);
            ReceptorVotosPrx receptorPrx = ReceptorVotosPrx.checkedCast(proxy);
            
            if (receptorPrx == null) {
                return false;
            }
            
            VotingSystem.Voto votoIce = convertirVotoJavaAIce(voto);
            return receptorPrx.recibirVoto(votoIce);
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
