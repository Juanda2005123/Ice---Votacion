package controller;

import model.Voto;
import model.Candidato;
import ui.VotacionUI;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Controlador principal para el manejo de votaciones
 * Implementa la lógica de negocio y coordina entre UI y datos
 */
public class ControllerVotacion {
    private VotacionUI ui;
    private String mesaId;
    private List<Voto> votosRegistrados;
    private Set<String> cedulasUsadas;
    private List<Candidato> candidatosDisponibles;
    
    public ControllerVotacion(String mesaId) {
        this.ui = new VotacionUI();
        this.mesaId = mesaId;
        this.votosRegistrados = new ArrayList<>();
        this.cedulasUsadas = new HashSet<>();
        this.candidatosDisponibles = inicializarCandidatos();
    }
    /**
     * Inicializa la lista de candidatos disponibles
     */
    private List<Candidato> inicializarCandidatos() {
        List<Candidato> candidatos = new ArrayList<>();
        
        // Candidatos principales con sus partidos políticos
        candidatos.add(new Candidato("CAND_001", "Juan Carlos Pérez", "Partido Liberal"));
        candidatos.add(new Candidato("CAND_002", "María Elena González", "Partido Conservador"));
        candidatos.add(new Candidato("CAND_003", "Roberto Sánchez Díaz", "Partido Verde"));
        candidatos.add(new Candidato("CAND_004", "Ana María Torres", "Movimiento Ciudadano"));
        candidatos.add(new Candidato("CAND_005", "Carlos Eduardo Ramírez", "Partido de la U"));
        
        // Opciones especiales
        candidatos.add(new Candidato("BLANCO", "Voto en Blanco"));
        
        return candidatos;
    }
    
    /**
     * Método principal que ejecuta el flujo de la aplicación
     */
    public void iniciar() {
        boolean continuar = true;
        
        ui.mostrarMensajeInfo("Sistema de votacion iniciado - Mesa: " + mesaId);
        ui.limpiarPantalla();
        
        while (continuar) {
            try {
                int opcion = ui.mostrarMenuPrincipal();
                ui.limpiarPantalla();
                
                switch (opcion) {
                    case 1:
                        procesarVoto();
                        break;
                    case 2:
                        ui.mostrarMensajeInfo("Cerrando sistema de votación...");
                        continuar = false;
                        break;
                    default:
                        ui.mostrarMensajeError("Opción inválida. Por favor seleccione 1 o 2.");
                        ui.pausar();
                        ui.limpiarPantalla();
                }
            } catch (Exception e) {
                ui.mostrarMensajeError("Error inesperado: " + e.getMessage());
                ui.pausar();
                ui.limpiarPantalla();
            }
        }
        
        ui.cerrar();
    }
    
    /**
     * Procesa el registro de un nuevo voto
     */
    private void procesarVoto() {
        try {
            // 1. Capturar cédula
            String cedula = ui.capturarCedula();
            
            // 2. Verificar que no haya votado antes
            if (cedulasUsadas.contains(cedula)) {
                ui.mostrarMensajeError("Esta cedula ya ha sido utilizada para votar.");
                ui.pausar();
                ui.limpiarPantalla();
                return;
            }
            
            // 3. Mostrar candidatos disponibles
            ui.mostrarCandidatos(candidatosDisponibles);
            
            // 4. Capturar selección
            int seleccion = ui.capturarSeleccionCandidato(candidatosDisponibles.size());
            
            // 5. Obtener candidato seleccionado
            Candidato candidatoSeleccionado = candidatosDisponibles.get(seleccion - 1);
            
            // 6. Confirmar voto
            if (!ui.confirmarVoto(candidatoSeleccionado, cedula)) {
                ui.mostrarMensajeInfo("Voto cancelado.");
                ui.pausar();
                ui.limpiarPantalla();
                return;
            }
            
            // 7. Registrar voto
            Voto nuevoVoto = new Voto(candidatoSeleccionado, LocalDateTime.now(), mesaId);
            votosRegistrados.add(nuevoVoto);
            cedulasUsadas.add(cedula);
            
            // 8. Aquí enviaríamos el voto al servidor central via Ice
            // Por ahora solo mostramos confirmación local
            ui.mostrarMensajeExito("Voto registrado exitosamente.");
            ui.mostrarMensajeInfo("Candidato: " + candidatoSeleccionado.getNombreCompleto());
            ui.mostrarMensajeInfo("Hora: " + nuevoVoto.getFechaHoraFormateada());
            
            // TODO: Implementar envío a Servidor Central con Ice
            // boolean ackRecibido = enviarVotoAlServidor(nuevoVoto);
            // if (!ackRecibido) {
            //     ui.mostrarMensajeError("Error al comunicarse con el servidor central");
            // }
            
        } catch (IllegalArgumentException e) {
            ui.mostrarMensajeError(e.getMessage());
        } catch (Exception e) {
            ui.mostrarMensajeError("Error durante el proceso de votación: " + e.getMessage());
        }
        
        ui.pausar();
        ui.limpiarPantalla();
    }
    
    // TODO: Métodos para integración con Ice
    
    /**
     * Envía un voto al servidor central usando Ice
     * @param voto El voto a enviar
     * @return true si se recibió ACK del servidor
     */
    private boolean enviarVotoAlServidor(Voto voto) {
        // TODO: Implementar comunicación Ice
        // 1. Obtener proxy del servidor central
        // 2. Enviar voto
        // 3. Esperar ACK (Reliable Message Pattern)
        // 4. Retornar resultado
        
        // Simulación por ahora
        try {
            Thread.sleep(100); // Simular latencia de red
            return true; // Simular ACK exitoso
        } catch (InterruptedException e) {
            return false;
        }
    }
    
    // Getters para testing y debugging
    public int getTotalVotosRegistrados() {
        return votosRegistrados.size();
    }
    
    public boolean cedulaYaUsada(String cedula) {
        return cedulasUsadas.contains(cedula);
    }
      public List<Candidato> getCandidatosDisponibles() {
        return new ArrayList<>(candidatosDisponibles);
    }
    
    public String getMesaId() {
        return mesaId;
    }
}
