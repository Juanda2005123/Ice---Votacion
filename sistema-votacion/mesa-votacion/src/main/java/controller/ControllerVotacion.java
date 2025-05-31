package controller;

import model.Voto;
import precarga.ConfiguracionMesa;
import precarga.SistemaPrecarga;
import model.Candidato;
import model.Votante;
import ui.VotacionUI;
import votos.CoordinadorEnvioVotos;
import votos.RepositorioMesaVotacion;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Controlador principal de votacion para gestion de mesa de votacion.
 * Implementa logica de negocio y coordina entre capas de UI y datos.
 * 
 * Este controlador maneja:
 * - Validacion de elegibilidad de votantes usando busquedas rapidas HashMap (complejidad O(1))
 * - Procesamiento y registro de votos con trazabilidad completa de auditoria
 * - Comunicacion con servidor central (futura implementacion Ice)
 * - Patron de mensaje confiable para confirmaciones de votos
 * - Manejo comprehensivo de errores y retroalimentacion al usuario
 * 
 * Arquitectura:
 * - Sigue patron MVC separando UI, logica de negocio y datos
 * - Usa Cadena de Responsabilidad para pasos de validacion
 * - Implementa patron Mensaje Confiable para comunicacion con servidor
 * - Disenado para escenarios de votacion de alto rendimiento (millones de votos en <3 segundos)
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-05-30
 */
public class ControllerVotacion {
    // ===== VARIABLES DE INSTANCIA =====
    private VotacionUI ui;                              // Manejador de interfaz de usuario
    private String idMesaVotacion;                      // Identificador unico para esta mesa de votacion
    private RepositorioMesaVotacion repositorio;        // Repositorio centralizado de datos de votacion
    private CoordinadorEnvioVotos coordinadorEnvio;     // Encargado de coordinar envio de votos
    
    // ===== CONSTRUCTOR =====
    /**
     * Constructor para ControllerVotacion.
     * Inicializa UI, estructuras de datos de votacion y carga votantes elegibles.
     * 
     * @param idMesaVotacion Identificador unico para esta mesa de votacion
     */
    public ControllerVotacion(String idMesaVotacion) {
        this.ui = new VotacionUI();
        this.idMesaVotacion = idMesaVotacion;
        
        // Inicializar repositorio centralizado y coordinador
        this.repositorio = new RepositorioMesaVotacion(idMesaVotacion);
        this.coordinadorEnvio = new CoordinadorEnvioVotos(repositorio);

        // Cargar datos iniciales
        SistemaPrecarga sistemaPrecarga = new SistemaPrecarga(repositorio);
        // Realizar precarga de datos POR AHORA, LUEGO SE HARA DESDE SERVIDOR Y SE ELIMINARA, ES PARA PRUEBAS LOCALES
        realizarPrecarga(sistemaPrecarga);
    }
    
    // ===== METODOS DE INICIALIZACION =====
    
    /**
     * Realiza la precarga de datos para la mesa de votacion.
     * Por ahora usa, datos simulados, luego se obtendra del servidor central cuando se implemente.
     */
    @SuppressWarnings("IncompleteIceConnection")
    private void realizarPrecarga(SistemaPrecarga sistemaPrecarga) {
        try {
            
            // Por ahora, usar configuracion simulada
            ConfiguracionMesa config = sistemaPrecarga.generarConfiguracionSimulada(idMesaVotacion);
            
            // Precargar datos
            sistemaPrecarga.precargarMesa(config);
            
            ui.mostrarMensajeInfo("Mesa precargada exitosamente:");
            ui.mostrarMensajeInfo("- Candidatos: " + repositorio.getCandidatosDisponibles().size());
            ui.mostrarMensajeInfo("- Votantes elegibles: " + repositorio.getTotalVotantesElegibles());
            
        } catch (Exception e) {
            ui.mostrarMensajeError("Error durante la precarga: " + e.getMessage());
            throw new RuntimeException("No se pudo precargar la mesa de votacion");
        }
    }
    
    // ===== FLUJO PRINCIPAL DE APLICACION =====
    
    /**
     * Metodo principal que ejecuta el flujo de la aplicacion.
     * Muestra opciones de menu y maneja interacciones del usuario hasta el apagado del sistema.
     */
    public void iniciar() {
        boolean continuarEjecutando = true;
        
        ui.mostrarMensajeInfo("Sistema de votacion iniciado - Mesa de Votacion: " + idMesaVotacion);
        ui.limpiarPantalla();
        
        while (continuarEjecutando) {
            try {
                int opcion = ui.mostrarMenuPrincipal();
                ui.limpiarPantalla();
                
                switch (opcion) {
                    case 1:
                        procesarVoto();
                        break;
                    case 2:
                        ui.mostrarMensajeInfo("Cerrando sistema de votacion...");
                        continuarEjecutando = false;
                        break;
                    default:
                        ui.mostrarMensajeError("Opcion invalida. Por favor seleccione 1 o 2.");
                        ui.pausarEjecucion();
                        ui.limpiarPantalla();
                }
            } catch (Exception e) {
                ui.mostrarMensajeError("Error inesperado: " + e.getMessage());
                ui.pausarEjecucion();
                ui.limpiarPantalla();
            }
        }
        
        ui.cerrar();
    }
    
    // ===== METODOS DE VALIDACION =====
    
    /**
     * Valida la elegibilidad del votante para esta mesa de votacion.
     * Verifica si el votante esta registrado y asignado a esta mesa.
     * 
     * @param cedula El numero de cedula del votante
     * @return Objeto Votante si es elegible, null si no se encuentra
     */
    private Votante validarElegibilidadVotante(String cedula) {
        return repositorio.obtenerVotantePorCedula(cedula);
    }
    
    /**
     * Valida el estado de votacion de un votante.
     * Verifica si el votante ya ha emitido su voto.
     * 
     * @param votante El votante a validar
     * @return true si el votante puede votar, false si ya voto
     */
    private boolean validarEstadoVotacion(Votante votante) {
        return !votante.isYaVoto();
    }
    
    /**
     * Confirma y procesa el voto completo.
     * Delega toda la responsabilidad al coordinador de envio.
     * 
     * @param votante El votante que emitio el voto
     * @param voto El voto que fue emitido
     * @throws IllegalArgumentException si hay error de validacion
     * @throws RuntimeException si hay error en el procesamiento
     */
    private void confirmarVoto(Votante votante, Voto voto) {
        // El coordinador se encarga de TODO: marcar votante, guardar voto, enviar
        coordinadorEnvio.procesarVotoCompleto(voto, votante);
    }
    
    // ===== PROCESAMIENTO DE VOTOS =====
    
    /**
     * Procesa el registro de un nuevo voto.
     * Implementa el flujo completo de votacion con validacion y confirmacion.
     * 
     * Flujo de trabajo:
     * 1. Capturar cedula del votante
     * 2. Validar elegibilidad del votante
     * 3. Validar estado de votacion
     * 4. Mostrar opciones de candidatos
     * 5. Capturar seleccion de voto
     * 6. Confirmar voto con votante
     * 7. Registrar voto y actualizar estado del votante
     * 8. Enviar a servidor central (implementacion futura)
     */
    private void procesarVoto() {
        try {
            // 1. Capturar cedula
            String cedula = ui.capturarCedula();
            
            // 2. Validar elegibilidad del votante
            Votante votante = validarElegibilidadVotante(cedula);
            if (votante == null) {
                ui.mostrarMensajeError("La cedula " + cedula + " no es elegible para votar en esta mesa de votacion.");
                ui.pausarEjecucion();
                ui.limpiarPantalla();
                return;
            }
            
            // 3. Validar estado de votacion
            if (!validarEstadoVotacion(votante)) {
                ui.mostrarMensajeError("El votante " + votante.getNombreCompleto() + " ya ha ejercido su derecho al voto.");
                ui.pausarEjecucion();
                ui.limpiarPantalla();
                return;
            }
            
            // 4. Mostrar informacion del votante
            ui.mostrarMensajeInfo("Votante elegible: " + votante.getNombreCompleto());
            ui.mostrarMensajeInfo("Mesa asignada: " + votante.getMesaId());
            
            // 5. Mostrar candidatos disponibles (desde repositorio)
            ui.mostrarCandidatos(repositorio.getCandidatosDisponibles());
            
            // 6. Capturar seleccion de candidato
            int seleccion = ui.capturarSeleccionCandidato(repositorio.getCandidatosDisponibles().size());
            
            // 7. Obtener candidato seleccionado
            Candidato candidatoSeleccionado = repositorio.getCandidatosDisponibles().get(seleccion - 1);
            
            // 8. Confirmar voto con informacion completa
            if (!ui.confirmarVoto(candidatoSeleccionado, votante)) {
                ui.mostrarMensajeInfo("Voto cancelado.");
                ui.pausarEjecucion();
                ui.limpiarPantalla();
                return;
            }
            
            // 9. Registrar voto
            Voto nuevoVoto = new Voto(candidatoSeleccionado, LocalDateTime.now(), idMesaVotacion);
            confirmarVoto(votante, nuevoVoto);
            
            // 10. Mostrar confirmacion de exito
            ui.mostrarMensajeExito("Voto registrado exitosamente.");
            ui.mostrarMensajeInfo("Votante: " + votante.getNombreCompleto());
            ui.mostrarMensajeInfo("Candidato: " + candidatoSeleccionado.getNombreCompleto());
            ui.mostrarMensajeInfo("Hora: " + nuevoVoto.getFechaHoraFormateada());
            
        } catch (IllegalArgumentException e) {
            ui.mostrarMensajeError(e.getMessage());
        } catch (RuntimeException e) {
            ui.mostrarMensajeError("Error durante el proceso de votacion: " + e.getMessage());
        } catch (Exception e) {
            ui.mostrarMensajeError("Error inesperado durante el proceso de votacion: " + e.getMessage());
        }
        
        ui.pausarEjecucion();
        ui.limpiarPantalla();
    }
    
    // ===== METODOS GETTER (Para pruebas y depuracion) =====
    
    /**
     * Obtiene el numero total de votos registrados.
     * 
     * @return Total de votos emitidos en esta mesa de votacion
     */
    public int getTotalVotosRegistrados() {
        return repositorio.getTotalVotos();
    }
    
    /**
     * Verifica si un votante ya ha votado.
     * 
     * @param cedula Numero de cedula del votante
     * @return true si el votante ya voto, false en caso contrario
     */
    public boolean yaVotoElVotante(String cedula) {
        Votante votante = repositorio.obtenerVotantePorCedula(cedula);
        return votante != null && votante.isYaVoto();
    }
    
    /**
     * Verifica si un votante es elegible para esta mesa de votacion.
     * 
     * @param cedula Numero de cedula del votante
     * @return true si el votante es elegible, false en caso contrario
     */
    public boolean esVotanteElegible(String cedula) {
        return repositorio.esVotanteElegible(cedula);
    }
    
    /**
     * Obtiene un votante por su numero de cedula.
     * 
     * @param cedula Numero de cedula del votante
     * @return Objeto Votante o null si no se encuentra
     */
    public Votante obtenerVotante(String cedula) {
        return repositorio.obtenerVotantePorCedula(cedula);
    }
    
    /**
     * Obtiene el numero total de votantes elegibles.
     * 
     * @return Total de votantes elegibles para esta mesa de votacion
     */
    public int getTotalVotantesElegibles() {
        return repositorio.getTotalVotantesElegibles();
    }
    
    /**
     * Obtiene el numero total de votantes que ya han votado.
     * 
     * @return Conteo de votantes que han emitido sus votos
     */
    public int getTotalVotantesQueYaVotaron() {
        return repositorio.getTotalVotantesQueYaVotaron();
    }
    
    /**
     * Obtiene una copia de la lista de candidatos disponibles.
     * 
     * @return Lista de candidatos disponibles
     */
    public List<Candidato> getCandidatosDisponibles() {
        return repositorio.getCandidatosDisponibles();
    }
    
    /**
     * Obtiene el ID de la mesa de votacion.
     * 
     * @return Identificador unico de esta mesa de votacion
     */
    public String getIdMesaVotacion() {
        return idMesaVotacion;
    }
}