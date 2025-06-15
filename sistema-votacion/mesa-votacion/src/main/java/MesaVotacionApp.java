import controller.ControllerVotacion;

/**
 * Aplicacion principal para Mesa de Votacion.
 * Utiliza arquitectura MVC para separar responsabilidades entre UI, logica de negocio y datos.
 * 
 * Esta aplicacion proporciona:
 * - Interfaz de usuario para registro de votos a traves de consola
 * - Validacion de cedulas y prevencion de votos duplicados
 * - Registro local de votos con trazabilidad de auditoria
 * - Verificacion de elegibilidad de votantes para mesa asignada
 * - Futura integracion con Servidor Central via middleware Ice
 * 
 * Componentes de Arquitectura:
 * - ControllerVotacion: Logica de negocio principal y coordinacion de flujo
 * - VotacionUI: Interfaz de usuario y manejo de entrada/salida
 * - Clases del modelo: Voto, Candidato, Votante para representacion de datos
 * 
 * Uso:
 * java MesaVotacionApp [idMesaVotacion]
 * 
 * @author Equipo Sistema de Votacion
 * @version 1.0
 * @since 2025-05-30
 */
public class MesaVotacionApp {
    
    /**
     * Punto de entrada de la aplicacion.
     * Inicializa el controlador de votacion y comienza el flujo principal de la aplicacion.
     * 
     * Argumentos de Linea de Comandos:
     * @param args[0] (opcional): ID de la mesa de votacion
     *                           Si no se proporciona, por defecto es "MESA-001"
     *                           
     * Codigos de Salida:
     * - 0: Terminacion normal
     * - 1: Error fatal de aplicacion
     */
    public static void main(String[] args) {
        
        System.out.println("=== SISTEMA DE VOTACION - MESA DE VOTACION ===");
        
        
        try {
            // Crear el controlador principal con el ID de mesa especificado
            ControllerVotacion controlador = new ControllerVotacion();
            
            // Iniciar el flujo principal de la aplicacion
            controlador.iniciar();
            
        } catch (Exception e) {
            // Manejar cualquier error fatal durante inicializacion o ejecucion
            System.err.println("ERROR FATAL DE APLICACION: " + e.getMessage());
            System.err.println("El sistema de mesa de votacion no puede continuar la operacion.");
            e.printStackTrace();
            System.exit(1);
        }
        
        // Terminacion normal de la aplicacion
        System.out.println("\n=== APAGADO DEL SISTEMA ===");
        System.out.println("Aplicacion de Mesa de Votacion terminada exitosamente.");
        System.out.println("Todos los datos de votacion han sido preservados.");
    }
}