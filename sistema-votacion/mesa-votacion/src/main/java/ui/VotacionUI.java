package ui;

import model.Candidato;
import model.Ciudadano;
import java.util.Scanner;
import java.util.List;

/**
 * Interfaz de Usuario para el sistema de votacion.
 * Maneja toda la interaccion con el usuario a traves de la consola.
 * 
 * Esta clase proporciona:
 * - Visualizacion del menu principal y captura de opciones
 * - Captura de documentos con validacion
 * - Interfaz de seleccion de candidatos
 * - Flujo de confirmacion de votos
 * - Mensajes de retroalimentacion (exito, error, informacion)
 * - Utilidades de gestion de pantalla
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-05-30
 */
public class VotacionUI {
    private Scanner scanner;
    
    /**
     * Constructor para VotacionUI.
     * Inicializa el Scanner para la entrada del usuario.
     */
    public VotacionUI() {
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Muestra el menu principal y captura la opcion seleccionada.
     * 
     * @return La opcion del menu seleccionada (1-2), o -1 para entrada invalida
     */
    public int mostrarMenuPrincipal() {
        System.out.println("=== MESA DE VOTACION ===");
        System.out.println("1. Registrar voto");
        System.out.println("2. Salir");
        System.out.print("Seleccione una opcion: ");
        
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1; // Opcion invalida
        }
    }
    
    /**
     * Captura el numero de documento del votante con validacion basica.
     * Valida longitud (6-15 digitos) y formato numerico.
     * 
     * @return El numero de documento validado
     * @throws IllegalArgumentException si el formato del documento es invalido
     */
    public String capturarDocumento() {
        System.out.print("Ingrese numero de documento: ");
        String documento = scanner.nextLine().trim();
        
        // Validacion basica
        if (documento.length() < 6 || documento.length() > 15) {
            throw new IllegalArgumentException("El documento debe tener entre 6 y 15 digitos");
        }
        
        // Verificar que contenga solo numeros
        if (!documento.matches("\\d+")) {
            throw new IllegalArgumentException("El documento debe contener solo numeros");
        }
        
        return documento;
    }
    
    /**
     * Método de compatibilidad - redirige a capturarDocumento()
     * @deprecated Usar capturarDocumento() en su lugar
     */
    @Deprecated
    public String capturarCedula() {
        return capturarDocumento();
    }
    
    /**
     * Muestra la lista de candidatos disponibles.
     * Presenta los nombres de candidatos con sus numeros de seleccion correspondientes.
     * 
     * @param candidatos Lista de candidatos disponibles para votar
     */
    public void mostrarCandidatos(List<Candidato> candidatos) {
        System.out.println("\n=== CANDIDATOS DISPONIBLES ===");
        for (int i = 0; i < candidatos.size(); i++) {
            Candidato candidato = candidatos.get(i);
            System.out.println((i + 1) + ". " + candidato.getNombre() + 
                             " (" + candidato.getPartidoPolitico() + ")");
        }
    }
    
    /**
     * Captura la seleccion de candidato del usuario.
     * Valida que la seleccion este dentro del rango valido.
     * 
     * @param totalCandidatos Numero total de candidatos disponibles
     * @return El indice del candidato seleccionado (basado en 1)
     * @throws IllegalArgumentException si la seleccion es invalida o fuera de rango
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
     * Confirma el voto automaticamente sin interaccion del usuario.
     * Muestra informacion completa del voto y confirma automaticamente.
     * 
     * @param candidatoSeleccionado El candidato seleccionado por el votante
     * @param votante El votante que emite el voto
     * @return true siempre (confirmacion automatica)
     */
    public boolean confirmarVoto(Candidato candidatoSeleccionado, Ciudadano votante) {
        System.out.println("\n=== CONFIRMACION DE VOTO ===");
        System.out.println("Votante: " + votante.getNombreCompleto());
        System.out.println("Documento: " + votante.getDocumento());
        System.out.println("Mesa de Votacion: " + votante.getMesaId());
        System.out.println("Candidato: " + candidatoSeleccionado.getNombre());
        System.out.println("Partido Politico: " + candidatoSeleccionado.getPartidoPolitico());
        System.out.println("Voto confirmado automaticamente.");
        
        return true; // Confirmacion automatica
    }
    
    /**
     * Muestra un mensaje de exito al usuario.
     * 
     * @param mensaje El mensaje de exito a mostrar
     */
    public void mostrarMensajeExito(String mensaje) {
        System.out.println("Exito: " + mensaje);
    }
    
    /**
     * Muestra un mensaje de error al usuario.
     * 
     * @param mensaje El mensaje de error a mostrar
     */
    public void mostrarMensajeError(String mensaje) {
        System.out.println("Error: " + mensaje);
    }
    
    /**
     * Muestra un mensaje informativo al usuario.
     * 
     * @param mensaje El mensaje informativo a mostrar
     */
    public void mostrarMensajeInfo(String mensaje) {
        System.out.println("Info: " + mensaje);
    }
    
    /**
     * Pausa la ejecucion hasta que el usuario presione Enter.
     * Se usa para permitir que los usuarios lean los mensajes antes de continuar.
     */
    public void pausarEjecucion() {
        System.out.print("Presione Enter para continuar...");
        scanner.nextLine();
    }
    
    /**
     * Limpia la pantalla (simulado con lineas en blanco).
     * Proporciona separacion visual entre diferentes operaciones.
     */
    public void limpiarPantalla() {
        for (int i = 0; i < 3; i++) {
            System.out.println();
        }
    }
    
    /**
     * Cierra el recurso scanner.
     * Debe ser llamado cuando la interfaz ya no sea necesaria.
     */
    public void cerrar() {
        if (scanner != null) {
            scanner.close();
        }
    }
}