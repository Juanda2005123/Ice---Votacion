package comunicacion;

import VotingSystem.*;
import config.ConfiguracionMesaVotacion;

/**
 * Cliente Ice para enviar deltas al BROKER implementando patrón Map-Reduce.
 * La mesa envía solo deltas consolidados al broker.
 * Garantiza una única instancia de conexión por mesa de votación.
 */
public class ServicioComunicacionIce {
    
    // Campos de la clase
    private com.zeroc.Ice.Communicator communicator;
    private BrokerServicePrx brokerProxy;
    private ConfiguracionMesaVotacion config;
    
    /**
     * Constructor para inicializar conexión con el broker.
     * Inicializa conexión usando configuración externa.
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
            
            config = new ConfiguracionMesaVotacion(rutaConfig);
            
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
            
        } catch (Exception e) {
            throw new RuntimeException("No se pudo inicializar conexión al broker");
        }
    }      /**
     * Envía un delta de conteos al broker (implementación Map-Reduce).
     * Método principal para envío de deltas en lugar de votos individuales.
     * 
     * @param delta DeltaConteo con conteos incrementales desde último envío
     * @return true si se envió exitosamente al broker
     */
    public boolean enviarDelta(DeltaConteo delta) {
        try {
            // Enviar delta al broker usando la nueva interfaz
            boolean resultado = brokerProxy.recibirDeltaConteo(delta);
            return resultado;
            
        } catch (Exception e) {
            return false;
        }
    }/**
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
            return 4; // Error de conexión se considera como "no existe"
        }
    }
      /**
     * Cierra la conexión con el broker Ice.
     */
    public void cerrarConexion() {
        if (communicator != null) {
            communicator.destroy();
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
}
