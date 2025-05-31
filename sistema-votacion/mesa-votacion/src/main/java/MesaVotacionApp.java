
import controller.ControllerVotacion;

/**
 * Aplicación principal para Mesa de Votación
 * Utiliza arquitectura MVC para separar responsabilidades
 * 
 * Esta aplicación:
 * - Maneja la interfaz de usuario para registro de votos
 * - Valida cédulas y evita votos duplicados
 * - Registra votos localmente
 * - Se comunicará con el Servidor Central via Ice (pendiente)
 */
public class MesaVotacionApp {
    
    /**
     * Punto de entrada de la aplicación
     * @param args Argumentos de línea de comandos
     *             args[0] (opcional): ID de la mesa (por defecto: "MESA-001")
     */
    public static void main(String[] args) {
        // Obtener ID de mesa desde argumentos o usar valor por defecto
        String mesaId = "MESA-001";
        
        try {
            // Crear e iniciar el controlador principal
            ControllerVotacion controller = new ControllerVotacion(mesaId);
            controller.iniciar();
            
        } catch (Exception e) {
            System.err.println("Error fatal en la aplicación: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        
        System.out.println("Aplicación Mesa de Votación finalizada correctamente.");
    }
}