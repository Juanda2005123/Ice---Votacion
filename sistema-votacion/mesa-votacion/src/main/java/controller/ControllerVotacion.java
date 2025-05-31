package controller;

import model.Voto;
import model.Candidato;
import model.Votante;
import ui.VotacionUI;

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
    private List<Voto> votosRegistrados;                // Lista de todos los votos emitidos en esta mesa
    private Map<String, Votante> votantesElegibles;     // Mapa por cedula para busqueda O(1) de votantes
    private List<Candidato> candidatosDisponibles;      // Lista de candidatos disponibles para votar
    
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
        this.votosRegistrados = new ArrayList<>();
        this.votantesElegibles = new HashMap<>();
        this.candidatosDisponibles = inicializarCandidatos();
        
        // Cargar lista de votantes elegibles para esta mesa de votacion
        cargarVotantesElegibles();
    }
    
    // ===== METODOS DE INICIALIZACION =====
    
    /**
     * Inicializa la lista de candidatos disponibles para esta eleccion.
     * En una implementacion real, esto vendria de una base de datos central de elecciones.
     * 
     * @return Lista de candidatos disponibles para votar
     */
    private List<Candidato> inicializarCandidatos() {
        List<Candidato> candidatos = new ArrayList<>();
        
        // Candidatos principales con sus partidos politicos
        candidatos.add(new Candidato("CAND_001", "Juan Carlos Perez", "Partido Liberal"));
        candidatos.add(new Candidato("CAND_002", "Maria Elena Gonzalez", "Partido Conservador"));
        candidatos.add(new Candidato("CAND_003", "Roberto Sanchez Diaz", "Partido Verde"));
        candidatos.add(new Candidato("CAND_004", "Ana Maria Torres", "Movimiento Ciudadano"));
        candidatos.add(new Candidato("CAND_005", "Carlos Eduardo Ramirez", "Partido de la Unidad"));
        
        // Opciones especiales de votacion
        candidatos.add(new Candidato("BLANCO", "Voto en Blanco"));
        
        return candidatos;
    }
    
    /**
     * Carga la lista de votantes elegibles para esta mesa de votacion.
     * En una implementacion real, esto vendria de una base de datos central o servicio.
     * Implementa busqueda rapida usando HashMap para validacion O(1) de votantes.
     */
    private void cargarVotantesElegibles() {
        // Simular carga de votantes desde una fuente de datos
        // En produccion, esto vendria del servidor central o base de datos
        
        List<Votante> votantes = new ArrayList<>();
        
        // Datos de muestra para esta mesa de votacion
        votantes.add(new Votante("12345678", "Ana", "Garcia Lopez", idMesaVotacion));
        votantes.add(new Votante("23456789", "Carlos", "Rodriguez Perez", idMesaVotacion));
        votantes.add(new Votante("34567890", "Maria", "Fernandez Torres", idMesaVotacion));
        votantes.add(new Votante("45678901", "Jose", "Martinez Ramirez", idMesaVotacion));
        votantes.add(new Votante("56789012", "Laura", "Gonzalez Diaz", idMesaVotacion));
        votantes.add(new Votante("67890123", "Pedro", "Hernandez Silva", idMesaVotacion));
        votantes.add(new Votante("78901234", "Sofia", "Lopez Morales", idMesaVotacion));
        votantes.add(new Votante("89012345", "Miguel", "Castro Vargas", idMesaVotacion));
        votantes.add(new Votante("90123456", "Elena", "Ruiz Mendoza", idMesaVotacion));
        votantes.add(new Votante("01234567", "Diego", "Jimenez Ortega", idMesaVotacion));
        
        // Agregar al mapa para busqueda rapida por numero de cedula
        for (Votante votante : votantes) {
            votantesElegibles.put(votante.getCedula(), votante);
        }
        
        ui.mostrarMensajeInfo("Cargados " + votantes.size() + " votantes elegibles para mesa de votacion " + idMesaVotacion);
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
        return votantesElegibles.get(cedula);
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
     * Actualiza el estado del votante despues del registro exitoso del voto.
     * Marca al votante como habiendo votado y registra el voto.
     * 
     * @param votante El votante que emitio el voto
     * @param voto El voto que fue emitido
     */
    private void actualizarEstadoVotante(Votante votante, Voto voto) {
        votante.marcarComoVotado();
        votosRegistrados.add(voto);
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
            
            // 5. Mostrar candidatos disponibles
            ui.mostrarCandidatos(candidatosDisponibles);
            
            // 6. Capturar seleccion de candidato
            int seleccion = ui.capturarSeleccionCandidato(candidatosDisponibles.size());
            
            // 7. Obtener candidato seleccionado
            Candidato candidatoSeleccionado = candidatosDisponibles.get(seleccion - 1);
            
            // 8. Confirmar voto con informacion completa
            if (!ui.confirmarVoto(candidatoSeleccionado, votante)) {
                ui.mostrarMensajeInfo("Voto cancelado.");
                ui.pausarEjecucion();
                ui.limpiarPantalla();
                return;
            }
            
            // 9. Registrar voto
            Voto nuevoVoto = new Voto(candidatoSeleccionado, LocalDateTime.now(), idMesaVotacion);
            actualizarEstadoVotante(votante, nuevoVoto);
            
            // 10. Mostrar confirmacion de exito
            ui.mostrarMensajeExito("Voto registrado exitosamente.");
            ui.mostrarMensajeInfo("Votante: " + votante.getNombreCompleto());
            ui.mostrarMensajeInfo("Candidato: " + candidatoSeleccionado.getNombreCompleto());
            ui.mostrarMensajeInfo("Hora: " + nuevoVoto.getFechaHoraFormateada());
            
            // TODO: Implementar envio a Servidor Central con Ice
            // boolean ackRecibido = enviarVotoAServidor(nuevoVoto);
            // if (!ackRecibido) {
            //     ui.mostrarMensajeError("Error comunicandose con el servidor central");
            // }
            
        } catch (IllegalArgumentException e) {
            ui.mostrarMensajeError(e.getMessage());
        } catch (Exception e) {
            ui.mostrarMensajeError("Error durante el proceso de votacion: " + e.getMessage());
        }
        
        ui.pausarEjecucion();
        ui.limpiarPantalla();
    }
    
    // ===== METODOS DE COMUNICACION (Futura Implementacion Ice) =====
    
    /**
     * Envia un voto al servidor central usando middleware Ice.
     * Implementa el patron Mensaje Confiable con confirmacion.
     * Este metodo sera implementado cuando se agregue comunicacion Ice.
     * 
     * @param voto El voto a enviar
     * @return true si se recibio ACK del servidor, false en caso contrario
     */
    @SuppressWarnings("unused")
    private boolean enviarVotoAServidor(Voto voto) {
        // TODO: Implementar comunicacion Ice
        // 1. Obtener proxy del servidor central
        // 2. Enviar voto
        // 3. Esperar ACK (Patron Mensaje Confiable)
        // 4. Retornar resultado
        
        // Simulacion por ahora
        try {
            Thread.sleep(100); // Simular latencia de red
            return true; // Simular ACK exitoso
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    // ===== METODOS GETTER (Para pruebas y depuracion) =====
    
    /**
     * Obtiene el numero total de votos registrados.
     * 
     * @return Total de votos emitidos en esta mesa de votacion
     */
    public int getTotalVotosRegistrados() {
        return votosRegistrados.size();
    }
    
    /**
     * Verifica si un votante ya ha votado.
     * 
     * @param cedula Numero de cedula del votante
     * @return true si el votante ya voto, false en caso contrario
     */
    public boolean yaVotoElVotante(String cedula) {
        Votante votante = votantesElegibles.get(cedula);
        return votante != null && votante.isYaVoto();
    }
    
    /**
     * Verifica si un votante es elegible para esta mesa de votacion.
     * 
     * @param cedula Numero de cedula del votante
     * @return true si el votante es elegible, false en caso contrario
     */
    public boolean esVotanteElegible(String cedula) {
        return votantesElegibles.containsKey(cedula);
    }
    
    /**
     * Obtiene un votante por su numero de cedula.
     * 
     * @param cedula Numero de cedula del votante
     * @return Objeto Votante o null si no se encuentra
     */
    public Votante obtenerVotante(String cedula) {
        return votantesElegibles.get(cedula);
    }
    
    /**
     * Obtiene el numero total de votantes elegibles.
     * 
     * @return Total de votantes elegibles para esta mesa de votacion
     */
    public int getTotalVotantesElegibles() {
        return votantesElegibles.size();
    }
    
    /**
     * Obtiene el numero total de votantes que ya han votado.
     * 
     * @return Conteo de votantes que han emitido sus votos
     */
    public int getTotalVotantesQueYaVotaron() {
        return (int) votantesElegibles.values().stream()
                .filter(Votante::isYaVoto)
                .count();
    }
    
    /**
     * Obtiene una copia de la lista de candidatos disponibles.
     * 
     * @return Lista de candidatos disponibles
     */
    public List<Candidato> getCandidatosDisponibles() {
        return new ArrayList<>(candidatosDisponibles);
    }
    
    /**
     * Obtiene el ID de la mesa de votacion.
     * 
     * @return Identificador unico de esta mesa de votacion
     */
    public String getIdMesaVotacion() {
        return idMesaVotacion;
    }
    
    /**
     * Obtiene estadisticas de votacion para esta mesa de votacion.
     * Util para reportes y monitoreo.
     * 
     * @return Cadena formateada con estadisticas de votacion
     */
    public String getEstadisticasVotacion() {
        int totalElegibles = getTotalVotantesElegibles();
        int totalVotaron = getTotalVotantesQueYaVotaron();
        int restantes = totalElegibles - totalVotaron;
        double porcentajeParticipacion = totalElegibles > 0 ? (double) totalVotaron / totalElegibles * 100 : 0;
        
        return String.format(
            "Estadisticas de Votacion para Mesa %s:\n" +
            "- Total votantes elegibles: %d\n" +
            "- Votos emitidos: %d\n" +
            "- Votantes restantes: %d\n" +
            "- Participacion: %.2f%%",
            idMesaVotacion, totalElegibles, totalVotaron, restantes, porcentajeParticipacion
        );
    }
}