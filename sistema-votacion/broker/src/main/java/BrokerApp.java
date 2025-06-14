import config.ConfiguracionBroker;
import controller.BrokerController;
import comunicacion.ServidorIceBroker;
import com.zeroc.Ice.*;

/**
 * Aplicacion principal del broker
 */
public class BrokerApp {
    
    private static Communicator communicator;
    private static ConfiguracionBroker config;
    private static BrokerController controller;
    
    public static void main(String[] args) {
        try {
            System.out.println("=== INICIANDO BROKER DE VOTACION ===");
            
            // Determinar ruta del archivo de configuracion
            String rutaConfig = args.length > 0 ? args[0] : 
                               "src/main/resources/broker.properties";
            
            // Cargar configuracion
            config = new ConfiguracionBroker(rutaConfig);
            System.out.println("Configuracion cargada exitosamente");
            
            // Inicializar Ice communicator
            communicator = Util.initialize(args);
            System.out.println("Ice communicator inicializado");
            
            // Crear controlador del broker
            controller = new BrokerController(config);
            
            // Crear servidor Ice
            ServidorIceBroker servidor = new ServidorIceBroker(controller);
            
            // Configurar adaptador de objetos
            String endpoints = String.format("tcp -h %s -p %d", 
                                           config.getHost(), config.getPuerto());
            
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints(
                "BrokerAdapter", endpoints
            );
            
            // Registrar el servant
            adapter.add(servidor, Util.stringToIdentity("BrokerService"));
            
            // Activar el adaptador
            adapter.activate();
            
            System.out.println("=== BROKER INICIADO EXITOSAMENTE ===");
            System.out.println("ID: " + config.getBrokerId());
            System.out.println("Nombre: " + config.getBrokerNombre());
            System.out.println("Escuchando en: " + config.getHost() + ":" + config.getPuerto());
            System.out.println("Destinos configurados: " + config.getTodosLosDestinos().size());
            System.out.println("Destinos activos: " + config.getDestinosActivos().size());
            
            // Verificar estado inicial de destinos
            controller.verificarEstadoDestinos();
            
            System.out.println("Presione Ctrl+C para detener el broker...");
            
            // Configurar shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n=== DETENIENDO BROKER ===");
                if (controller != null) {
                    controller.cerrar();
                }
                if (communicator != null) {
                    communicator.destroy();
                }
                System.out.println("Broker detenido correctamente");
            }));
            
            // Mantener el broker corriendo
            communicator.waitForShutdown();
            
        } catch (java.lang.Exception e) {  // Especificar java.lang.Exception
            System.err.println("Error iniciando broker: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
