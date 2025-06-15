import config.ConfiguracionBroker;
import controller.BrokerController;
import comunicacion.ServidorIceBroker;
import com.zeroc.Ice.*;

/**
 * Aplicacion principal del broker de votacion.
 * 
 * Esta clase es responsable de:
 * - Inicializar la configuracion del broker desde archivos externos o internos
 * - Crear y configurar el servidor Ice para recibir votos
 * - Inicializar el controlador del broker
 * - Verificar conectividad con destinos configurados
 * - Mantener el broker en funcionamiento
 * 
 * El broker actua como intermediario entre mesas de votacion y lugares de votacion,
 * reenviando votos segun la estrategia de enrutamiento configurada.
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-06-14
 */
public class BrokerApp {
    
    private static Communicator communicator;
    private static ConfiguracionBroker config;
    private static BrokerController controller;
    
    /**
     * Metodo principal que inicia el broker de votacion.
     * 
     * El proceso de inicializacion incluye:
     * 1. Carga de configuracion (externa o interna)
     * 2. Inicializacion del communicator Ice
     * 3. Creacion del controlador y servidor Ice
     * 4. Verificacion de conectividad con destinos
     * 5. Activacion del servicio
     * 
     * @param args Argumentos de linea de comandos. Si se proporciona un argumento,
     *             se usa como ruta al archivo de configuracion externa.
     */
    public static void main(String[] args) {          
        try {
            // Determinar ruta del archivo de configuracion (externo o interno)
            String rutaConfig;
            if (args.length > 0) {
                rutaConfig = args[0];  // Archivo externo pasado como parametro
            } else {
                // Buscar archivo externo en directorio actual
                java.io.File archivoExterno = new java.io.File("broker.properties");
                if (archivoExterno.exists()) {
                    rutaConfig = "broker.properties";
                } else {
                    rutaConfig = "src/main/resources/broker.properties";  // Archivo interno
                }
            }
            
            // Cargar configuracion del broker
            config = new ConfiguracionBroker(rutaConfig);
            
            // Inicializar Ice communicator
            communicator = Util.initialize(args);
            
            // Crear controlador del broker
            controller = new BrokerController(config);
            
            // Crear servidor Ice para recibir votos
            ServidorIceBroker servidor = new ServidorIceBroker(controller);
            
            // Configurar adaptador de objetos Ice
            String endpoints = String.format("tcp -h %s -p %d", 
                                           config.getHost(), config.getPuerto());
            
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints(
                "BrokerAdapter", endpoints
            );
            
            // Registrar el servant para el servicio BrokerService
            adapter.add(servidor, Util.stringToIdentity("BrokerService"));
            
            // Activar el adaptador para comenzar a recibir peticiones
            adapter.activate();
            
            // Mensaje de inicio del broker (sin tildes)
            System.out.println("=== BROKER " + config.getBrokerId() + " INICIADO ===");
            System.out.println("- ID: " + config.getBrokerId());
            System.out.println("- Nombre: " + config.getBrokerNombre());
            System.out.println("- Puerto: " + config.getPuerto());
            
            // Verificar conectividad con todos los destinos configurados
            controller.verificarConectividadInicial();
            
            System.out.println("Broker listo para procesar votos...");
            
            // Configurar shutdown hook para cierre limpio
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Cerrando broker...");
                if (controller != null) {
                    controller.cerrar();
                }
                if (communicator != null) {
                    communicator.destroy();
                }
            }));
            
            // Mantener el broker corriendo hasta recibir senal de cierre
            communicator.waitForShutdown();
            
        } catch (java.lang.Exception e) {  // Especificar java.lang.Exception
            System.err.println("Error iniciando broker: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
