package comunicacion;

import VotingSystem.*;
import model.Voto;
import config.ConfiguracionMesa;

/**
 * Cliente Ice para enviar votos al BROKER implementando patrón Singleton.
 * La mesa envía votos al broker, quien los reenvía a los destinos finales.
 * Garantiza una única instancia de conexión por mesa de votación.
 */
public class ServicioComunicacionIce {
    
    
    // Campos de la clase
    private com.zeroc.Ice.Communicator communicator;
    private BrokerServicePrx brokerProxy;
    private ConfiguracionMesa config;
    
    /**
     * Constructor privado para implementar Singleton.
     * Inicializa conexión con el broker usando configuración externa.
     */
    public ServicioComunicacionIce() {        
        try {
            // Cargar configuración externa (buscar archivo externo primero)
            String rutaConfig;
            java.io.File archivoExterno = new java.io.File("mesa-votacion.properties");
            if (archivoExterno.exists()) {
                rutaConfig = "mesa-votacion.properties";
            } else {
                rutaConfig = "src/main/resources/mesa-votacion.properties";
            }
            
            config = new ConfiguracionMesa(rutaConfig);
            
            // Inicializar Ice communicator
            communicator = com.zeroc.Ice.Util.initialize();
            
            // Crear proxy al broker usando configuración
            String proxyString = String.format("BrokerService:tcp -h %s -p %d", 
                                             config.getBrokerHost(), config.getBrokerPuerto());
            com.zeroc.Ice.ObjectPrx proxy = communicator.stringToProxy(proxyString);
            brokerProxy = BrokerServicePrx.checkedCast(proxy);
            
            if (brokerProxy == null) {
                throw new RuntimeException("No se pudo conectar al broker en " + 
                                         config.getBrokerHost() + ":" + config.getBrokerPuerto());
            }
            
            System.out.println("ServicioComunicacionIce: Conectado al broker " + 
                             config.getBrokerHost() + ":" + config.getBrokerPuerto());
            
        } catch (Exception e) {
            System.err.println("Error inicializando conexión al broker: " + e.getMessage());
            throw new RuntimeException("No se pudo inicializar conexión al broker");
        }
    }
    
    /**
     * Envía un voto al broker de forma simple y directa.
     * Solo envía el voto, sin información adicional del votante.
     * 
     * @param voto El voto a enviar
     * @return true si se envió exitosamente
     */
    public boolean enviarVoto(Voto voto) {
        try {
            // Convertir voto Java a Ice
            VotingSystem.Voto votoIce = convertirVotoJavaAIce(voto);
            
            // Enviar al broker de forma simple
            brokerProxy.recibirVoto(votoIce);
            
            return true; // Siempre exitoso si llega al broker
            
        } catch (Exception e) {
            return false;
        }
    }    /**
     * Valida un voto enviando documento y candidato al broker.
     * 
     * @param documento Documento del votante
     * @param candidatoId ID del candidato elegido
     * @return Código de validación: 0=puede votar, 1=no es su mesa, 2=ya votó, 3=no existe
     */
    public Integer validarVoto(String documento, Integer candidatoId) {
        try {
            return brokerProxy.recibirValidacionVotante(documento, candidatoId);
        } catch (Exception e) {
            return 3; // Error de conexión se considera como "no existe"
        }
    }
    
    /**
     * Cierra la conexión con el broker Ice.
     * También resetea la instancia Singleton para permitir reconexión.
     */
    public void cerrarConexion() {
        if (communicator != null) {
            communicator.destroy();
            System.out.println("Conexión al broker cerrada");
        }
        
    }
    
    /**
     * Verifica la conectividad con el broker.
     * 
     * @return true si el broker responde
     */
    public boolean verificarConectividad() {
        try {
            return brokerProxy != null && brokerProxy.ping();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Realiza un ping al broker para verificar disponibilidad.
     * 
     * @return true si el broker está disponible
     */
    public boolean ping() {
        return verificarConectividad();
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
}
