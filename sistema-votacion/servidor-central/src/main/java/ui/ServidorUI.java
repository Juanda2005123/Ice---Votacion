package ui;

import java.util.Map;
import java.util.Scanner;

/**
 * Interfaz de usuario simple para el servidor central.
 * Permite ver estadísticas básicas de votación.
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-05-30
 */
public class ServidorUI {
    
    private Scanner scanner;
    
    /**
     * Constructor de la interfaz de usuario.
     */
    public ServidorUI() {
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Muestra el banner inicial del servidor.
     */
    public void mostrarBanner() {
        System.out.println("==========================================");
        System.out.println("         SERVIDOR CENTRAL DE VOTACION    ");
        System.out.println("              Sistema Electoral          ");
        System.out.println("==========================================");
        System.out.println("Servidor iniciado correctamente");
        System.out.println("Esperando conexiones de mesas de votacion...\n");
    }
    
    /**
     * Muestra el menú principal del servidor.
     * 
     * @return Opción seleccionada por el usuario
     */
    public int mostrarMenuPrincipal() {
        System.out.println("\n=== MENU SERVIDOR CENTRAL ===");
        System.out.println("1. Ver total de votos recibidos");
        System.out.println("2. Ver votos por candidato");
        System.out.println("3. Cerrar servidor");
        System.out.print("Seleccione una opcion: ");
        
        try {
            return scanner.nextInt();
        } catch (Exception e) {
            scanner.nextLine(); // Limpiar buffer
            return -1; // Opción inválida
        }
    }
    
    /**
     * Muestra el total de votos recibidos.
     * 
     * @param totalVotos Número total de votos
     */
    public void mostrarTotalVotos(int totalVotos) {
        System.out.println("\n=== TOTAL DE VOTOS ===");
        System.out.println("Votos recibidos: " + totalVotos);
        pausarEjecucion();
    }
    
    /**
     * Muestra las estadísticas de votos por candidato.
     * 
     * @param votosPorCandidato Mapa con candidato y número de votos
     * @param totalVotos Total de votos para calcular porcentajes
     */
    public void mostrarVotosPorCandidato(Map<String, Integer> votosPorCandidato, int totalVotos) {
        System.out.println("\n=== VOTOS POR CANDIDATO ===");
        
        if (votosPorCandidato.isEmpty()) {
            System.out.println("No hay votos registrados aun.");
        } else {
            System.out.println("+----------------------------------+---------+-------------+");
            System.out.println("| CANDIDATO                        | VOTOS   | PORCENTAJE  |");
            System.out.println("+----------------------------------+---------+-------------+");
            
            // Ordenar por número de votos (descendente)
            votosPorCandidato.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> {
                    String candidato = entry.getKey();
                    int votos = entry.getValue();
                    double porcentaje = totalVotos > 0 ? (double) votos / totalVotos * 100 : 0;
                    
                    System.out.printf("| %-32s | %7d | %9.2f%% |%n", 
                        truncar(candidato, 32), votos, porcentaje);
                });
            
            System.out.println("+----------------------------------+---------+-------------+");
            System.out.printf("TOTAL: %d votos registrados%n", totalVotos);
        }
        
        pausarEjecucion();
    }
    
    /**
     * Muestra un mensaje de información.
     * 
     * @param mensaje Mensaje a mostrar
     */
    public void mostrarMensajeInfo(String mensaje) {
        System.out.println("[INFO] " + mensaje);
    }
    
    /**
     * Muestra un mensaje de error.
     * 
     * @param mensaje Mensaje de error
     */
    public void mostrarMensajeError(String mensaje) {
        System.err.println("[ERROR] " + mensaje);
    }
    
    /**
     * Muestra un mensaje de éxito.
     * 
     * @param mensaje Mensaje de éxito
     */
    public void mostrarMensajeExito(String mensaje) {
        System.out.println("[EXITO] " + mensaje);
    }
    
    /**
     * Pausa la ejecución hasta que el usuario presione Enter.
     */
    public void pausarEjecucion() {
        System.out.print("\nPresione Enter para continuar...");
        scanner.nextLine();
        try {
            scanner.nextLine();
        } catch (Exception e) {
            // Ignorar errores del scanner
        }
    }
    
    /**
     * Limpia la pantalla de la consola.
     */
    public void limpiarPantalla() {
        // Para Windows
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (Exception e) {
            // Si falla, imprimir líneas vacías
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }
    
    /**
     * Trunca un texto si es muy largo.
     * 
     * @param texto Texto a truncar
     * @param maxLength Longitud máxima
     * @return Texto truncado si es necesario
     */
    private String truncar(String texto, int maxLength) {
        if (texto.length() <= maxLength) {
            return texto;
        }
        return texto.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Cierra el scanner.
     */
    public void cerrar() {
        if (scanner != null) {
            scanner.close();
        }
    }
}