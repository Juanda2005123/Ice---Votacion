import controller.ControllerServidor;

/**
 * Aplicación principal del servidor central de votación.
 * Punto de entrada del sistema.
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-05-30
 */
public class ServidorCentralApp {
    
    /**
     * Método principal de la aplicación.
     * 
     * @param args Argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        try {
            // Crear e iniciar el controlador del servidor
            ControllerServidor servidor = new ControllerServidor();
            servidor.iniciar();
            
        } catch (Exception e) {
            System.err.println("Error crítico al iniciar el servidor: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}