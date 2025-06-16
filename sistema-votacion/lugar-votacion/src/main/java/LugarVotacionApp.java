import config.ConfiguracionLugar;
import controller.LugarController;
import comunicacion.ServidorIceLugar;
import com.zeroc.Ice.*;

/**
 * Aplicacion principal del lugar de votacion.
 * 
 * Esta clase es responsable de:
 * - Inicializar la configuracion del lugar desde archivos externos o internos
 * - Crear y configurar el servidor Ice para recibir votos
 * - Inicializar el controlador del lugar
 * - Verificar conectividad con el broker destino
 * - Mantener el lugar en funcionamiento
 * 
 * El lugar actua como intermediario entre el broker mesa-lugar y el broker lugar-departamento,
 * reenviando votos sin procesamiento adicional.
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-06-14
 */
public class LugarVotacionApp {
    
    private static Communicator communicator;
    private static ConfiguracionLugar config;
    private static LugarController controller;
    
    /**
     * Metodo principal que inicia el lugar de votacion.
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
                java.io.File archivoExterno = new java.io.File("lugar-votacion.properties");
                if (archivoExterno.exists()) {
                    rutaConfig = "lugar-votacion.properties";
                } else {
                    rutaConfig = "src/main/resources/lugar-votacion.properties";  // Archivo interno
                }
            }
            
            // Cargar configuracion
            config = new ConfiguracionLugar(rutaConfig);
            
            // Inicializar Ice communicator
            communicator = Util.initialize(args);
            
            // Crear controlador del lugar de votacion
            controller = new LugarController(config);
            
            // Crear servidor Ice para recibir votos del broker mesa-lugar
            ServidorIceLugar servidor = new ServidorIceLugar(controller);
            
            // Configurar adaptador de objetos para recibir votos
            String endpoints = String.format("tcp -h %s -p %d", 
                                           config.getHost(), config.getPuerto());
            
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints(
                "LugarVotacionAdapter", endpoints
            );
            
            // Registrar el servant como ReceptorVotos (para recibir del broker)
            adapter.add(servidor, Util.stringToIdentity("ReceptorVotos"));
            
            // Activar el adaptador
            adapter.activate();
              // Mensaje de inicio silencioso para automatizacion
            System.out.println("LUGAR " + config.getLugarId() + " INICIADO");
            
            // Verificar conectividad con broker destino
            controller.verificarConectividadInicial();
            
            // Configurar shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Cerrando lugar de votacion...");
                if (controller != null) {
                    controller.cerrar();
                }
                if (communicator != null) {
                    communicator.destroy();
                }
            }));
            
            // Mantener el lugar de votacion corriendo
            communicator.waitForShutdown();
              } catch (java.lang.Exception e) {  // Especificar java.lang.Exception
            System.err.println("Error iniciando lugar de votacion: " + e.getMessage());
            System.exit(1);
        }
    }
}
