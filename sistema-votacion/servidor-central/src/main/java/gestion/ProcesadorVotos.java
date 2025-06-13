package gestion;

import controller.ControllerServidor;
import model.Voto;
import model.Ciudadano;

/**
 * Procesador de votos que maneja la lógica de procesamiento antes del almacenamiento.
 * Actúa como intermediario entre ServicioComunicacionIce y ControllerServidor.
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-05-30
 */
public class ProcesadorVotos {
    
    private ControllerServidor controllerServidor;
    
    /**
     * Constructor del procesador de votos.
     * 
     * @param controllerServidor Controlador del servidor para almacenar datos
     */
    public ProcesadorVotos(ControllerServidor controllerServidor) {
        if (controllerServidor == null) {
            throw new IllegalArgumentException("El controlador del servidor no puede ser null");
        }
        this.controllerServidor = controllerServidor;
    }
    
    /**
     * Procesa un voto recibido vía Ice.
     * 
     * @param voto Voto a procesar
     * @param mesaId ID de la mesa que envía el voto
     * @return true si el voto fue procesado exitosamente
     */
    public boolean procesarVoto(Voto voto, String mesaId) {
        try {
            // Validaciones básicas
            if (voto == null) {
                throw new IllegalArgumentException("El voto no puede ser null");
            }
            
            if (mesaId == null || mesaId.trim().isEmpty()) {
                throw new IllegalArgumentException("El ID de mesa no puede ser null o vacío");
            }
            
            // Log del voto recibido
            controllerServidor.getUI().mostrarMensajeInfo(
                String.format("Voto recibido de mesa %s - Candidato: %s", 
                    mesaId, voto.getCandidato().getNombreCompleto()));
            
            // Enviar al controlador para almacenar
            return controllerServidor.almacenarVoto(voto);
            
        } catch (Exception e) {
            controllerServidor.getUI().mostrarMensajeError("Error procesando voto: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Procesa información de un votante recibida vía Ice.
     * 
     * @param votante Votante a procesar
     * @param mesaId ID de la mesa que envía la información
     * @return true si el votante fue procesado exitosamente
     */
    public boolean procesarVotante(Ciudadano votante, String mesaId) {
        try {
            // Validaciones básicas
            if (votante == null) {
                throw new IllegalArgumentException("El votante no puede ser null");
            }
            
            if (mesaId == null || mesaId.trim().isEmpty()) {
                throw new IllegalArgumentException("El ID de mesa no puede ser null o vacío");
            }
            
            // Log del votante recibido
            controllerServidor.getUI().mostrarMensajeInfo(
                String.format("Votante recibido de mesa %s", 
                    mesaId));
            
            // Enviar al controlador para almacenar
            return controllerServidor.almacenarVotante(votante);
            
        } catch (Exception e) {
            controllerServidor.getUI().mostrarMensajeError("Error procesando votante: " + e.getMessage());
            return false;
        }
    }
    /**
     * Obtiene referencia al controlador para consultas.
     * 
     * @return ControllerServidor
     */
    public ControllerServidor getControllerServidor() {
        return controllerServidor;
    }
}