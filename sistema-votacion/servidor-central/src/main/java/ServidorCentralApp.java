import controller.ServidorController;
import config.ConfiguracionServidor;
import comunicacion.ServidorIceServidor;
import com.zeroc.Ice.*;

/**
 * Aplicacion principal del servidor central de votacion.
 * 
 * Esta clase es responsable de:
 * - Inicializar la configuracion del servidor central desde archivos externos o internos
 * - Crear y configurar el servidor Ice para recibir votos
 * - Inicializar el controlador del servidor central
 * - Mantener el servidor central en funcionamiento
 * 
 * El servidor central es el destino final del flujo de votacion,
 * recibe votos y los imprime sin reenviarlos.
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-06-15
 */
public class ServidorCentralApp {
    
    private static Communicator communicator;
    private static ConfiguracionServidor config;
    private static ServidorController controller;
    
    /**
     * Metodo principal que inicia el servidor central de votacion.
     * 
     * @param args Argumentos de linea de comandos, puede incluir la ruta del archivo de configuracion
     */
    public static void main(String[] args) {
        try {
            // Determinar ruta del archivo de configuracion (externo o interno)
            String rutaConfig;
            if (args.length > 0) {
                rutaConfig = args[0];  // Archivo externo pasado como parametro
            } else {
                // Buscar archivo externo en directorio actual
                java.io.File archivoExterno = new java.io.File("servidor-central.properties");
                if (archivoExterno.exists()) {
                    rutaConfig = "servidor-central.properties";
                } else {
                    rutaConfig = "src/main/resources/servidor-central.properties";  // Archivo interno
                }
            }
            
            System.out.println("=== INICIANDO SERVIDOR CENTRAL DE VOTACION ===");
            
            // Cargar configuracion del servidor central
            config = new ConfiguracionServidor(rutaConfig);
            System.out.println("Configuracion cargada: " + config.getServidorNombre() + " (ID: " + config.getServidorId() + ")");
            
            // Inicializar Ice communicator
            communicator = Util.initialize(args);
            
            // Crear controlador del servidor central
            controller = new ServidorController(config);
            
            // Crear servidor Ice para recibir votos del departamento
            ServidorIceServidor servidor = new ServidorIceServidor(controller);
            
            // Configurar adaptador de objetos para recibir votos
            String endpoints = String.format("tcp -h %s -p %d", 
                                           config.getHost(), config.getPuerto());
            
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints(
                "ServidorCentralAdapter", endpoints
            );
            
            // Registrar el servant como ReceptorVotos (para recibir del departamento)
            adapter.add(servidor, Util.stringToIdentity("ReceptorVotos"));
            
            // Activar el adaptador
            adapter.activate();
            
            // Mensaje de inicio del servidor central
            System.out.println("=== SERVIDOR CENTRAL " + config.getServidorId() + " INICIADO ===");
            System.out.println("- ID: " + config.getServidorId());
            System.out.println("- Nombre: " + config.getServidorNombre());
            System.out.println("- Puerto de recepcion: " + config.getPuerto());
            System.out.println("\n");
            System.out.println("Servidor central listo para recibir votos...");
            
            // Configurar shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Cerrando servidor central...");
                if (communicator != null) {
                    communicator.destroy();
                }
            }));
            
            // Mantener el servidor central corriendo
            communicator.waitForShutdown();
            
        } catch (java.lang.Exception e) {  // Especificar java.lang.Exception
            System.err.println("Error critico al iniciar el servidor central: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}