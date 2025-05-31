package ui;

import model.Candidato;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

/**
 * Interfaz de usuario para el sistema de votación
 * Maneja toda la interacción con el usuario a través de consola
 */
public class VotacionUI {
    private Scanner scanner;
    
    public VotacionUI() {
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Muestra el menú principal y captura la opción seleccionada
     */
    public int mostrarMenuPrincipal() {
        System.out.println("=== MESA DE VOTACION ===");
        System.out.println("1. Registrar voto");
        System.out.println("2. Salir");
        System.out.print("Seleccione una opcion: ");
        
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1; // Opción inválida
        }
    }
      /**
     * Captura la cédula del votante
     */
    public String capturarCedula() {
        System.out.print("Ingrese numero de cedula: ");
        String cedula = scanner.nextLine().trim();
        
        // Validación básica
        if (cedula.length() < 6 || cedula.length() > 15) {
            throw new IllegalArgumentException("La cedula debe tener entre 6 y 15 digitos");
        }
        
        // Verificar que solo contenga números
        if (!cedula.matches("\\d+")) {
            throw new IllegalArgumentException("La cedula debe contener solo numeros");
        }
        
        return cedula;
    }
      /**
     * Muestra la lista de candidatos disponibles
     */
    public void mostrarCandidatos(List<Candidato> candidatos) {
        System.out.println("\n=== CANDIDATOS DISPONIBLES ===");
        for (int i = 0; i < candidatos.size(); i++) {
            Candidato candidato = candidatos.get(i);
            
            System.out.println((i + 1) + ". " + candidato.getNombre());
            
        }
        
    }
      /**
     * Captura la selección del candidato
     */
    public int capturarSeleccionCandidato(int totalCandidatos) {
        System.out.print("Seleccione su candidato (numero): ");
        
        try {
            int seleccion = Integer.parseInt(scanner.nextLine().trim());
            if (seleccion < 1 || seleccion > totalCandidatos) {
                throw new IllegalArgumentException("Seleccion fuera de rango");
            }
            return seleccion;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Debe ingresar un numero valido");
        }
    }      
    /**
     * Confirma el voto antes de registrarlo
     */
    public boolean confirmarVoto(Candidato candidatoSeleccionado, String cedula) {
        System.out.println("\n=== CONFIRMACION DE VOTO ===");
        System.out.println("Cedula: " + cedula);
        System.out.println("Candidato: " + candidatoSeleccionado.getNombreCompleto());
        if (!candidatoSeleccionado.esVotoEspecial()) {
            System.out.println("Partido: " + candidatoSeleccionado.getPartidoPolitico());
        }
        System.out.print("¿Confirma su voto? (S/N): ");
        
        String respuesta = scanner.nextLine().trim().toLowerCase();
        return respuesta.equals("s") || respuesta.equals("si");
    }
     
    
    /**
     * Muestra mensaje de éxito
     */
    public void mostrarMensajeExito(String mensaje) {
        System.out.println("Exito: " + mensaje);
    }
    
    /**
     * Muestra mensaje de error
     */
    public void mostrarMensajeError(String mensaje) {
        System.out.println("Error: " + mensaje);
    }
    
    /**
     * Muestra mensaje informativo
     */
    public void mostrarMensajeInfo(String mensaje) {
        System.out.println("Info: " + mensaje);
    }
    
    /**
     * Pausa la ejecución hasta que el usuario presione Enter
     */
    public void pausar() {
        System.out.print("Presione Enter para continuar...");
        scanner.nextLine();
    }
    
    /**
     * Limpia la pantalla (simulado con líneas en blanco)
     */
    public void limpiarPantalla() {
        for (int i = 0; i < 3; i++) {
            System.out.println();
        }
    }
    
    /**
     * Cierra el scanner
     */
    public void cerrar() {
        if (scanner != null) {
            scanner.close();
        }
    }
}
