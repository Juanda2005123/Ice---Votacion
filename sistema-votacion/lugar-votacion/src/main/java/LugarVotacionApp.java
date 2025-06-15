import config.ConfiguracionLugar;
import controller.LugarController;
import comunicacion.ServidorIceLugar;
import com.zeroc.Ice.*;

/**
 * Aplicación principal del lugar de votación
 * FUNCIÓN: Actúa como intermediario entre broker mesa-lugar y broker lugar-departamento
 */
public class LugarVotacionApp {
    
    private static Communicator communicator;
    private static ConfiguracionLugar config;
    private static LugarController controller;
    
    public static void main(String[] args) {          
        try {
            // Determinar ruta del archivo de configuración (externo o interno)
            String rutaConfig;
            if (args.length > 0) {
                rutaConfig = args[0];  // Archivo externo pasado como parámetro
            } else {
                // Buscar archivo externo en directorio actual
                java.io.File archivoExterno = new java.io.File("lugar-votacion.properties");
                if (archivoExterno.exists()) {
                    rutaConfig = "lugar-votacion.properties";
                } else {
                    rutaConfig = "src/main/resources/lugar-votacion.properties";  // Archivo interno
                }
            }
            
            // Cargar configuración
            config = new ConfiguracionLugar(rutaConfig);
            
            // Inicializar Ice communicator
            communicator = Util.initialize(args);
            
            // Crear controlador del lugar de votación
            controller = new LugarController(config);
            
            // Crear servidor Ice para RECIBIR votos del broker mesa-lugar
            ServidorIceLugar servidor = new ServidorIceLugar(controller);
            
            // Configurar adaptador de objetos para RECIBIR votos
            String endpoints = String.format("tcp -h %s -p %d", 
                                           config.getHost(), config.getPuerto());
            
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints(
                "LugarVotacionAdapter", endpoints
            );
            
            // Registrar el servant como ReceptorVotos (para recibir del broker)
            adapter.add(servidor, Util.stringToIdentity("ReceptorVotos"));
            
            // Activar el adaptador
            adapter.activate();
            
            // Verificar conectividad con broker lugar-departamento
            if (controller.verificarConectividadBroker()) {
                System.out.println("Conectividad con broker lugar-departamento: OK");
            } else {
                System.out.println("ADVERTENCIA: No se pudo conectar con broker lugar-departamento");
            }
            
            // Mensaje principal de inicio
            System.out.println("=== LUGAR DE VOTACIÓN " + config.getLugarId() + " INICIADO ===");
            System.out.println("- Recibiendo votos en puerto: " + config.getPuerto());
            System.out.println("- Reenviando votos a: " + config.getBrokerDestinoHost() + ":" + config.getBrokerDestinoPuerto());
            System.out.println("Lugar de votación listo para procesar votos...");
            
            // Configurar shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Cerrando lugar de votación...");
                if (controller != null) {
                    controller.cerrar();
                }
                if (communicator != null) {
                    communicator.destroy();
                }
            }));
            
            // Mantener el lugar de votación corriendo
            communicator.waitForShutdown();
            
        } catch (java.lang.Exception e) {  // Especificar java.lang.Exception
            System.err.println("Error iniciando lugar de votación: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
