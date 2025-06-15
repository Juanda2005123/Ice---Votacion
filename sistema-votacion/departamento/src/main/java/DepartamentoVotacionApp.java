import config.ConfiguracionDepartamento;
import controller.DepartamentoController;
import comunicacion.ServidorIceDepartamento;
import com.zeroc.Ice.*;

/**
 * Aplicacion principal del departamento de votacion.
 * 
 * Esta clase es responsable de:
 * - Inicializar la configuracion del departamento desde archivos externos o internos
 * - Crear y configurar el servidor Ice para recibir votos
 * - Inicializar el controlador del departamento
 * - Verificar conectividad con el broker destino (servidor central)
 * - Mantener el departamento en funcionamiento
 * 
 * El departamento actua como intermediario entre el broker lugar-departamento y el servidor central,
 * reenviando votos sin procesamiento adicional.
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-06-15
 */
public class DepartamentoVotacionApp {
    
    private static Communicator communicator;
    private static ConfiguracionDepartamento config;
    private static DepartamentoController controller;
    
    /**
     * Metodo principal que inicia el lugar de votacion.
     * 
     * @param args Argumentos de linea de comandos, puede incluir la ruta del archivo de configuracion
     */
    public static void main(String[] args) {          
        try {            // Determinar ruta del archivo de configuracion (externo o interno)
            String rutaConfig;
            if (args.length > 0) {
                rutaConfig = args[0];  // Archivo externo pasado como parametro
            } else {
                // Buscar archivo externo en directorio actual
                java.io.File archivoExterno = new java.io.File("departamento.properties");
                if (archivoExterno.exists()) {
                    rutaConfig = "departamento.properties";
                } else {
                    rutaConfig = "src/main/resources/departamento.properties";  // Archivo interno
                }
            }
            
            // Cargar configuracion
            config = new ConfiguracionDepartamento(rutaConfig);
            
            // Inicializar Ice communicator
            communicator = Util.initialize(args);
            
            // Crear controlador del departamento de votacion
            controller = new DepartamentoController(config);
            
            // Crear servidor Ice para recibir votos del broker lugar-departamento
            ServidorIceDepartamento servidor = new ServidorIceDepartamento(controller);
            
            // Configurar adaptador de objetos para recibir votos
            String endpoints = String.format("tcp -h %s -p %d", 
                                           config.getHost(), config.getPuerto());
            
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints(
                "DepartamentoVotacionAdapter", endpoints
            );
            
            // Registrar el servant como ReceptorVotos (para recibir del broker)
            adapter.add(servidor, Util.stringToIdentity("ReceptorVotos"));
            
            // Activar el adaptador
            adapter.activate();
            
            // Mensaje de inicio del departamento de votacion
            System.out.println("=== DEPARTAMENTO DE VOTACION " + config.getDepartamentoId() + " INICIADO ===");
            System.out.println("- ID: " + config.getDepartamentoId());
            System.out.println("- Nombre: " + config.getDepartamentoNombre());
            System.out.println("- Puerto de recepcion: " + config.getPuerto());
            
            // Verificar conectividad con broker destino
            controller.verificarConectividadInicial();            
            System.out.println("\n");
            System.out.println("Departamento de votacion listo para procesar votos...");
            
            // Configurar shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Cerrando departamento de votacion...");
                if (controller != null) {
                    controller.cerrar();
                }
                if (communicator != null) {
                    communicator.destroy();
                }
            }));
            
            // Mantener el departamento de votacion corriendo
            communicator.waitForShutdown();
              } catch (java.lang.Exception e) {  // Especificar java.lang.Exception
            System.err.println("Error iniciando departamento de votacion: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
