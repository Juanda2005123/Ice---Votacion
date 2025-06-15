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
            // Determinar ruta del archivo de configuracion (externo o interno)
            String rutaConfig;
            if (args.length > 0) {
                rutaConfig = args[0];  // Archivo externo
            } else {
                // Buscar archivo externo en directorio actual
                java.io.File archivoExterno = new java.io.File("broker.properties");
                if (archivoExterno.exists()) {
                    rutaConfig = "broker.properties";
                } else {
                    rutaConfig = "src/main/resources/broker.properties";  // Archivo interno
                }
            }
            
            // Cargar configuracion
            config = new ConfiguracionBroker(rutaConfig);
            
            // Inicializar Ice communicator
            communicator = Util.initialize(args);
            
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
            
            // Mensaje de inicio del broker
            System.out.println("=== BROKER " + config.getBrokerId() + " INICIADO ===");
            System.out.println("- ID: " + config.getBrokerId());
            System.out.println("- Nombre: " + config.getBrokerNombre());
            System.out.println("- Puerto: " + config.getPuerto());
            
            // VERIFICAR CONECTIVIDAD CON DESTINOS
            controller.verificarConectividadInicial();
            
            System.out.println("Broker listo para procesar votos...");
            
            // Configurar shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (controller != null) {
                    controller.cerrar();
                }
                if (communicator != null) {
                    communicator.destroy();
                }
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
