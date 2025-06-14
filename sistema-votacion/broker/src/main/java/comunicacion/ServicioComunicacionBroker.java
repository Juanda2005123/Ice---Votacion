package comunicacion;

import VotingSystem.*;
import model.Voto;
import model.Candidato;
import config.ConfiguracionBroker;
import enrutamiento.EstrategiaEnrutamiento;

/**
 * Servicio para ENVIAR votos a destinos configurados.
 * SIMPLE: solo reenvía votos, sin validaciones ni estadísticas.
 */
public class ServicioComunicacionBroker {
    
    private com.zeroc.Ice.Communicator communicator;
    private ConfiguracionBroker config;
    private EstrategiaEnrutamiento estrategiaEnrutamiento;
    
    public ServicioComunicacionBroker(ConfiguracionBroker config) {
        this.config = config;
        this.estrategiaEnrutamiento = new EstrategiaEnrutamiento(config);
        
        try {
            communicator = com.zeroc.Ice.Util.initialize();
            System.out.println("Cliente Ice del broker inicializado");
        } catch (Exception e) {
            throw new RuntimeException("Error inicializando cliente Ice: " + e.getMessage());
        }
    }
    
    /**
     * FUNCIÓN PRINCIPAL: Reenvía un voto al destino seleccionado
     * SIN validaciones - SOLO reenvío
     */
    public boolean reenviarVoto(Voto voto) {
        ConfiguracionBroker.Destino destino = estrategiaEnrutamiento.seleccionarDestino();
        
        if (destino == null) {
            System.err.println("No hay destinos disponibles");
            return false;
        }
        
        return enviarVotoADestino(voto, destino);
    }
    
    /**
     * Envía un voto a un destino específico con reintentos
     */
    private boolean enviarVotoADestino(Voto voto, ConfiguracionBroker.Destino destino) {
        int maxIntentos = config.getReintentos();
        
        for (int intento = 1; intento <= maxIntentos; intento++) {
            try {
                String proxyString = String.format("ReceptorVotos:tcp -h %s -p %d", 
                                                 destino.getHost(), destino.getPuerto());
                
                com.zeroc.Ice.ObjectPrx proxy = communicator.stringToProxy(proxyString);
                ReceptorVotosPrx receptorPrx = ReceptorVotosPrx.checkedCast(proxy);
                
                if (receptorPrx == null) {
                    throw new RuntimeException("No se pudo crear proxy al destino: " + destino.getId());
                }
                
                VotingSystem.Voto votoIce = convertirVotoJavaAIce(voto);
                boolean resultado = receptorPrx.recibirVoto(votoIce);
                
                if (resultado) {
                    System.out.println("Voto enviado exitosamente a " + destino.getId());
                    return true;
                }
                
            } catch (Exception e) {
                System.err.println("Error enviando voto (intento " + intento + "/" + maxIntentos + 
                                 ") a " + destino.getId() + ": " + e.getMessage());
                
                if (intento < maxIntentos) {
                    try { Thread.sleep(1000); } catch (InterruptedException ie) { break; }
                }
            }
        }
        
        System.err.println("FALLÓ: No se pudo enviar el voto a " + destino.getId());
        return false;
    }
    
    /**
     * Verifica conectividad con todos los destinos
     */
    public void verificarConectividadDestinos() {
        System.out.println("=== VERIFICANDO DESTINOS ===");
        
        for (ConfiguracionBroker.Destino destino : config.getTodosLosDestinos()) {
            boolean conectado = verificarConectividadDestino(destino);
            System.out.println("Destino " + destino.getId() + " -> " + 
                             (conectado ? "CONECTADO" : "DESCONECTADO"));
        }
    }
    
    /**
     * Verifica conectividad con un destino específico
     */
    public boolean verificarConectividadDestino(ConfiguracionBroker.Destino destino) {
        try {
            String proxyString = String.format("ReceptorVotos:tcp -h %s -p %d", 
                                             destino.getHost(), destino.getPuerto());
            
            com.zeroc.Ice.ObjectPrx proxy = communicator.stringToProxy(proxyString);
            ReceptorVotosPrx receptorPrx = ReceptorVotosPrx.checkedCast(proxy);
            
            return receptorPrx != null && receptorPrx.ping();
            
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
            System.out.println("Conexión Ice del broker cerrada");
        }
    }
}
