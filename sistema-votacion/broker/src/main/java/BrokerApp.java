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
        try {            // Determinar ruta del archivo de configuracion (siempre externo)
            String rutaConfig;
            if (args.length > 0) {
                rutaConfig = args[0];  // Archivo externo pasado como parametro
            } else {
                // Buscar archivo en directorio junto al JAR
                String jarDir = obtenerDirectorioJar();
                rutaConfig = jarDir + java.io.File.separator + "broker.properties";
                
                // Verificar que el archivo existe
                java.io.File archivoConfig = new java.io.File(rutaConfig);
                if (!archivoConfig.exists()) {
                    System.err.println("ERROR: No se encuentra el archivo de configuracion: " + rutaConfig);
                    System.err.println("Asegurese de que broker.properties este en el mismo directorio que el JAR.");
                    System.exit(1);
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
            adapter.activate();              // Mensaje de inicio del broker silencioso para automatizacion
            System.out.println("BROKER " + config.getBrokerId() + " INICIADO");
            
            // Verificar conectividad con todos los destinos configurados
            controller.verificarConectividadInicial();
            
            // Configurar shutdown hook para cierre limpio
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
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
            System.exit(1);
        }
    }
    
    /**
     * Obtiene el directorio donde se encuentra el JAR de la aplicacion.
     * 
     * @return Ruta absoluta del directorio del JAR
     */
    private static String obtenerDirectorioJar() {
        try {
            String rutaJar = BrokerApp.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI().getPath();
            
            java.io.File archivoJar = new java.io.File(rutaJar);
            if (archivoJar.isDirectory()) {
                // Corriendo desde directorio de clases (desarrollo)
                return archivoJar.getAbsolutePath();
            } else {
                // Corriendo desde JAR (produccion)
                return archivoJar.getParent();
            }
        } catch (java.net.URISyntaxException e) {
            // En caso de error, usar directorio actual
            return System.getProperty("user.dir");
        }
    }
}