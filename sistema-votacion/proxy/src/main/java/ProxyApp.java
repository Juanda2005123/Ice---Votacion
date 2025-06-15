import config.ConfiguracionProxy;
import controller.ProxyController;
import comunicacion.ServidorIceProxy;
import com.zeroc.Ice.*;

/**
 * Aplicacion principal del proxy de validacion.
 * 
 * Esta clase es responsable de:
 * - Inicializar la configuracion del proxy desde archivos externos o internos
 * - Crear y configurar el servidor Ice para recibir validaciones
 * - Inicializar el controlador del proxy
 * - Verificar conectividad con el nodo destino configurado
 * - Mantener el proxy en funcionamiento
 * 
 * El proxy actua como intermediario para validaciones de ciudadanos,
 * reenviando validaciones al nodo destino configurado.
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-06-15
 */
public class ProxyApp {
    
    private static Communicator communicator;
    private static ConfiguracionProxy config;
    private static ProxyController controller;
      /**
     * Metodo principal que inicia el proxy de validacion.
     * 
     * El proceso de inicializacion incluye:
     * 1. Carga de configuracion (externa o interna)
     * 2. Inicializacion del communicator Ice
     * 3. Creacion del controlador y servidor Ice
     * 4. Verificacion de conectividad con nodo destino
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
                java.io.File archivoExterno = new java.io.File("proxy.properties");
                if (archivoExterno.exists()) {
                    rutaConfig = "proxy.properties";
                } else {
                    rutaConfig = "src/main/resources/proxy.properties";  // Archivo interno
                }
            }
            
            // Cargar configuracion del proxy
            config = new ConfiguracionProxy(rutaConfig);
            
            // Inicializar Ice communicator
            communicator = Util.initialize(args);
            
            // Crear controlador del proxy
            controller = new ProxyController(config);
            
            // Crear servidor Ice para recibir validaciones
            ServidorIceProxy servidor = new ServidorIceProxy(controller);
            
            // Configurar adaptador de objetos Ice
            String endpoints = String.format("tcp -h %s -p %d", 
                                           config.getHost(), config.getPuerto());
            
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints(
                "ProxyAdapter", endpoints
            );
            
            // Registrar el servant para el servicio ReceptorVotos
            adapter.add(servidor, Util.stringToIdentity("ReceptorVotos"));
            
            // Activar el adaptador para comenzar a recibir peticiones
            adapter.activate();
            
            // Mensaje de inicio del proxy
            System.out.println("=== PROXY " + config.getProxyId() + " INICIADO ===");
            System.out.println("- ID: " + config.getProxyId());
            System.out.println("- Nombre: " + config.getProxyNombre());
            System.out.println("- Puerto: " + config.getPuerto());
            System.out.println("- Nodo Destino: " + config.getNodoDestinoHost() + ":" + config.getNodoDestinoPuerto());
            
            // Verificar conectividad con el nodo destino configurado
            controller.verificarConectividadInicial();
            
            System.out.println("Proxy listo para procesar validaciones...");
            
            // Configurar shutdown hook para cierre limpio
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Cerrando proxy...");
                if (controller != null) {
                    controller.cerrar();
                }
                if (communicator != null) {
                    communicator.destroy();
                }
            }));
            
            // Mantener el proxy corriendo hasta recibir senal de cierre
            communicator.waitForShutdown();
            
        } catch (java.lang.Exception e) {  // Especificar java.lang.Exception
            System.err.println("Error iniciando proxy: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
