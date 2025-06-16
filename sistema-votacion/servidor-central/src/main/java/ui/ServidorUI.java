package ui;

import controller.ServidorController;
import java.util.Scanner;
import java.util.Map;

/**
 * Interfaz de usuario para el servidor central de votación.
 * 
 * Esta clase proporciona:
 * - Menú principal con opciones de gestión
 * - Visualización del conteo nacional en tiempo real
 * - Timer para medir tiempo de operación (iniciar/parar)
 * - Generación de reporte CSV al salir
 * - Al salir, muestra el tiempo transcurrido como primera acción
 * 
 * Opciones del menú:
 * 1. Actualizar estadísticas
 * 2. Salir y generar reporte CSV
 * 3. Iniciar/Parar timer
 * 
 * @author Sistema de Votacion
 * @version 4.1 - Con Timer Completo
 * @since 2025-06-16
 */
public class ServidorUI {
      private ServidorController controller;
    private Scanner scanner;
    private boolean running;
    
    // Timer para medir tiempo de funcionamiento
    private long timerInicio;
    private boolean timerActivo;
    
    /**
     * Constructor que inicializa la UI con el controlador.
     * 
     * @param controller Controlador del servidor central
     */
    public ServidorUI(ServidorController controller) {
        this.controller = controller;
        this.scanner = new Scanner(System.in);
        this.running = true;
    }
    
    /**
     * Inicia la interfaz de usuario.
     */
    public void iniciar() {
        mostrarBienvenida();
        
        while (running) {
            mostrarMenu();
            procesarOpcion();
        }
        
        scanner.close();
    }
    
    /**
     * Muestra el mensaje de bienvenida.
     */
    private void mostrarBienvenida() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("    SERVIDOR CENTRAL DE VOTACIÓN");
        System.out.println("    Conteo Nacional en Tiempo Real");
        System.out.println("=".repeat(50));
    }
      /**
     * Muestra el menú principal.
     */
    private void mostrarMenu() {
        System.out.println("\n" + "-".repeat(30));
        System.out.println("MENÚ PRINCIPAL");
        System.out.println("-".repeat(30));
        mostrarEstadisticas();
        
        // Mostrar estado del timer
        if (timerActivo) {
            long tiempoTranscurrido = System.currentTimeMillis() - timerInicio;
            long segundos = tiempoTranscurrido / 1000;
            long minutos = segundos / 60;
            segundos = segundos % 60;
            System.out.printf("\nTimer activo: %02d:%02d\n", minutos, segundos);
        }
        
        System.out.println("\nOpciones:");
        System.out.println("1. Actualizar estadisticas");
        System.out.println("2. Salir y generar reporte CSV");
        System.out.println("3. " + (timerActivo ? "Parar timer" : "Iniciar timer"));
        System.out.print("\nSeleccione una opción: ");
    }
    
    /**
     * Muestra las estadísticas actuales del conteo nacional.
     */
    private void mostrarEstadisticas() {
        Map<Integer, Integer> conteo = controller.obtenerConteoNacional();
        int totalVotos = controller.obtenerTotalVotos();
        
        System.out.println("\nESTADISTICAS NACIONALES:");
        System.out.println("Total de votos: " + totalVotos);
        System.out.println("Candidatos: " + conteo.size());
        
        if (!conteo.isEmpty()) {
            System.out.println("\nConteo por candidato:");
            conteo.entrySet().stream()
                  .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                  .forEach(entry -> 
                      System.out.printf("  Candidato %d: %,d votos\n", 
                                      entry.getKey(), entry.getValue())
                  );
        } else {
            System.out.println("No hay votos registrados aún.");
        }
    }
      /**
     * Procesa la opción seleccionada por el usuario.
     */
    private void procesarOpcion() {
        try {
            String input = scanner.nextLine().trim();
            
            switch (input) {
                case "1":
                    // Las estadisticas se actualizan automaticamente
                    System.out.println("Estadisticas actualizadas.");
                    break;
                    
                case "2":
                    salir();
                    break;
                    
                case "3":
                    manejarTimer();
                    break;
                    
                default:
                    System.out.println("Opcion no valida. Intente nuevamente.");
                    break;
            }
        } catch (Exception e) {
            System.out.println("Error procesando opción: " + e.getMessage());
        }    }
    
    /**
     * Maneja el inicio/parada del timer.
     */
    private void manejarTimer() {
        if (timerActivo) {
            // Parar el timer
            long tiempoTranscurrido = System.currentTimeMillis() - timerInicio;
            long segundos = tiempoTranscurrido / 1000;
            long minutos = segundos / 60;
            segundos = segundos % 60;
            
            System.out.printf("\nTimer parado. Tiempo transcurrido: %02d:%02d\n", minutos, segundos);
            timerActivo = false;
        } else {
            // Iniciar el timer
            timerInicio = System.currentTimeMillis();
            timerActivo = true;
            System.out.println("\nTimer iniciado!");
        }
    }
    
    /**
     * Maneja la salida del sistema.
     */
    private void salir() {
        // PRIMERA ACCIÓN: Imprimir tiempo transcurrido si el timer estaba activo
        if (timerActivo) {
            long tiempoTranscurrido = System.currentTimeMillis() - timerInicio;
            long segundos = tiempoTranscurrido / 1000;
            long minutos = segundos / 60;
            long horas = minutos / 60;
            minutos = minutos % 60;
            segundos = segundos % 60;
            
            System.out.println("\n" + "=".repeat(50));
            System.out.printf("TIEMPO TOTAL DE FUNCIONAMIENTO: %02d:%02d:%02d\n", horas, minutos, segundos);
            System.out.println("=".repeat(50));
        }
        
        System.out.println("\n" + "=".repeat(40));
        System.out.println("CERRANDO SERVIDOR CENTRAL");
        System.out.println("=".repeat(40));
        
        // Mostrar estadísticas finales
        mostrarEstadisticas();
        
        System.out.println("\nGenerando reporte CSV...");
        controller.cerrarYGenerarReporte();
        
        System.out.println("!Servidor central cerrado exitosamente!");
        System.out.println("Reporte guardado como: resumen.csv");
        
        running = false;
        
        // Forzar salida del sistema
        System.exit(0);
    }
}
