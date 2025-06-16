import controller.ServidorController;
import config.ConfiguracionServidor;
import comunicacion.ServidorIceServidor;
import ui.ServidorUI;
import gestion.GestorCandidatos;
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
    private static GestorCandidatos gestorCandidatos;
    
    /**
     * Metodo principal que inicia el servidor central de votacion.
     * 
     * @param args Argumentos de linea de comandos, puede incluir la ruta del archivo de configuracion
     */
    public static void main(String[] args) {        try {
            // SOLO buscar archivo de configuracion externo junto al JAR
            String rutaConfig;
            if (args.length > 0) {
                rutaConfig = args[0];  // Archivo externo pasado como parametro
            } else {
                rutaConfig = "servidor-central.properties";  // Archivo externo en directorio actual
            }
            
            // Verificar que el archivo existe
            java.io.File archivoConfig = new java.io.File(rutaConfig);
            if (!archivoConfig.exists()) {
                System.err.println("ERROR: Archivo de configuracion no encontrado: " + rutaConfig);
                System.err.println("Por favor, coloque el archivo 'servidor-central.properties' junto al JAR");
                System.exit(1);
            }
            
            System.out.println("=== INICIANDO SERVIDOR CENTRAL DE VOTACION ===");
            System.out.println("Usando configuracion: " + archivoConfig.getAbsolutePath());
              // Cargar configuracion del servidor central
            config = new ConfiguracionServidor(rutaConfig);
            System.out.println("Configuracion cargada: " + config.getServidorNombre() + " (ID: " + config.getServidorId() + ")");
            
            // Cargar candidatos desde archivo externo
            String rutaCandidatos;
            if (args.length > 1) {
                rutaCandidatos = args[1];  // Archivo de candidatos pasado como segundo parametro
            } else {
                rutaCandidatos = "candidatos.csv";  // Archivo de candidatos en directorio actual
            }
            
            java.io.File archivoCandidatos = new java.io.File(rutaCandidatos);
            if (!archivoCandidatos.exists()) {
                System.err.println("ERROR: Archivo de candidatos no encontrado: " + rutaCandidatos);
                System.err.println("Por favor, coloque el archivo 'candidatos.csv' junto al JAR");
                System.exit(1);
            }
            
            System.out.println("Cargando candidatos desde: " + archivoCandidatos.getAbsolutePath());
            gestorCandidatos = new GestorCandidatos(rutaCandidatos);
            System.out.println(gestorCandidatos.getResumenCandidatos());
            
            // Inicializar Ice communicator
            communicator = Util.initialize(args);
              // Crear controlador del servidor central (pasando el gestor de candidatos)
            controller = new ServidorController(config, gestorCandidatos);
            
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
            System.out.println("Servidor central listo para recibir deltas departamentales...");
            
            // Iniciar UI en hilo separado
            ServidorUI ui = new ServidorUI(controller);
            Thread uiThread = new Thread(() -> ui.iniciar());
            uiThread.setDaemon(false);
            uiThread.start();
              // Configurar shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Cerrando servidor central...");
                if (controller != null) {
                    controller.cerrarYGenerarReporte();
                }
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