package controller;

import ui.ServidorUI;
import model.Voto;
import model.Votante;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import comunicacion.ServicioComunicacionIce;
import gestion.ProcesadorVotos;

/**
 * Controlador principal del servidor central.
 * Maneja la lógica de negocio y coordina con la UI.
 * Por ahora almacena datos en memoria.
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-05-30
 */
public class ControllerServidor {
    
    // ===== VARIABLES DE INSTANCIA =====
    private ServidorUI ui;
    
    
    // Almacenamiento temporal en memoria (thread-safe para Ice)
    private List<Voto> votosRecibidos;
    private Map<String, Votante> votantesQueVotaron; // Key: cédula
    private Set<String> votosProcesadosIds = ConcurrentHashMap.newKeySet();


    // Estadísticas calculadas
    private Map<String, Integer> conteosPorCandidato;
    private com.zeroc.Ice.Communicator communicator;
    private ServicioComunicacionIce servicioIce;
    
    // ===== CONSTRUCTOR =====
    
    /**
     * Constructor del controlador del servidor.
     */
    public ControllerServidor() {
        this.ui = new ServidorUI();
        
        // Inicializar estructuras thread-safe para Ice
        this.votosRecibidos = Collections.synchronizedList(new ArrayList<>());
        this.votantesQueVotaron = new ConcurrentHashMap<>();
        this.conteosPorCandidato = new ConcurrentHashMap<>();
    }
    
    // ===== FLUJO PRINCIPAL =====
    
    /**
     * Inicia el servidor y maneja el ciclo principal.
     */
    public void iniciar() {
        iniciarServidorIce();
        ui.mostrarBanner();
        ui.limpiarPantalla();
        
        boolean continuar = true;
        
        while (continuar) {
            try {
                int opcion = ui.mostrarMenuPrincipal();
                ui.limpiarPantalla();
                
                switch (opcion) {
                    case 1:
                        mostrarTotalVotos();
                        break;
                    case 2:
                        mostrarVotosPorCandidato();
                        break;
                    case 3:
                        ui.mostrarMensajeInfo("Cerrando servidor...");
                        continuar = false;
                        break;
                    default:
                        ui.mostrarMensajeError("Opción inválida. Seleccione 1, 2, o 3.");
                        ui.pausarEjecucion();
                        ui.limpiarPantalla();
                }
            } catch (Exception e) {
                ui.mostrarMensajeError("Error inesperado: " + e.getMessage());
                ui.pausarEjecucion();
                ui.limpiarPantalla();
            }
        }
        
        cerrarServidor();
    }

    private void iniciarServidorIce() {
        try {
            // Crear communicator
            communicator = com.zeroc.Ice.Util.initialize();
            
            // Crear adapter
            com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints(
                "VotingServiceAdapter", "tcp -p 10000");
            
            // Crear servant
            gestion.ProcesadorVotos procesador = new gestion.ProcesadorVotos(this);
            servicioIce = new comunicacion.ServicioComunicacionIce(procesador);
            
            // Agregar servant al adapter
            adapter.add(servicioIce, com.zeroc.Ice.Util.stringToIdentity("VotingService"));
            
            // Activar adapter
            adapter.activate();
            
            ui.mostrarMensajeInfo("Servidor Ice iniciado en puerto 10000");
            
        } catch (Exception e) {
            ui.mostrarMensajeError("Error iniciando servidor Ice: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    // ===== MÉTODOS DE VISUALIZACIÓN =====
    
    /**
     * Muestra el total de votos recibidos.
     */
    private void mostrarTotalVotos() {
        int total = getTotalVotos();
        ui.mostrarTotalVotos(total);
        ui.limpiarPantalla();
    }
    
    /**
     * Muestra las estadísticas de votos por candidato.
     */
    private void mostrarVotosPorCandidato() {
        Map<String, Integer> estadisticas = getEstadisticasPorCandidato();
        int total = getTotalVotos();
        ui.mostrarVotosPorCandidato(estadisticas, total);
        ui.limpiarPantalla();
    }
    
    // ===== MÉTODOS PARA PROCESAMIENTO (serán llamados por ProcesadorVotos) =====
    
    /**
     * Recibe un voto procesado por ProcesadorVotos.
     * 
     * @param voto Voto recibido
     * @return true si el voto fue almacenado exitosamente
     */
   public boolean almacenarVoto(Voto voto) {
        try {
            if (voto == null) throw new IllegalArgumentException("El voto no puede ser null");

            if (!votosProcesadosIds.add(voto.getVotoId())) {
                ui.mostrarMensajeError("Voto duplicado: " + voto.getVotoId());
                return false;
            }

            votosRecibidos.add(voto);
            String nombreCandidato = voto.getCandidato().getNombreCompleto();
            conteosPorCandidato.put(nombreCandidato,
                conteosPorCandidato.getOrDefault(nombreCandidato, 0) + 1);

            return true;

        } catch (Exception e) {
            ui.mostrarMensajeError("Error almacenando voto: " + e.getMessage());
            return false;
        }
    }

    
    /**
     * Recibe información de un votante procesado por ProcesadorVotos.
     * 
     * @param votante Votante que ha emitido su voto
     * @return true si la información fue almacenada exitosamente
     */
    public boolean almacenarVotante(Votante votante) {
        try {
            if (votante == null) {
                throw new IllegalArgumentException("El votante no puede ser null");
            }
            
            // Almacenar votante (sobrescribe si ya existe)
            votantesQueVotaron.put(votante.getCedula(), votante);
            
            return true;
            
        } catch (Exception e) {
            ui.mostrarMensajeError("Error almacenando votante: " + e.getMessage());
            return false;
        }
    }
    
    // ===== MÉTODOS DE CONSULTA =====
    
    /**
     * Obtiene el total de votos recibidos.
     * 
     * @return Número total de votos
     */
    public int getTotalVotos() {
        return votosRecibidos.size();
    }
    
    /**
     * Obtiene el total de votantes únicos que han votado.
     * 
     * @return Número total de votantes
     */
    public int getTotalVotantes() {
        return votantesQueVotaron.size();
    }
    
    /**
     * Obtiene las estadísticas de votos por candidato.
     * 
     * @return Mapa con candidato y número de votos
     */
    public Map<String, Integer> getEstadisticasPorCandidato() {
        // Retornar copia para evitar modificaciones externas
        return new HashMap<>(conteosPorCandidato);
    }
    
    // ===== MÉTODOS DE GESTIÓN =====
    
    /**
     * Cierra el servidor de manera limpia.
     */
    private void cerrarServidor() {
        if (servicioIce != null) {
            servicioIce.shutdownThreadPool();
        }

        // Cerrar Ice
        if (communicator != null) {
            communicator.destroy();
            ui.mostrarMensajeInfo("Servidor Ice cerrado.");
        }
        
        ui.mostrarMensajeInfo("Servidor cerrado correctamente.");
        ui.mostrarMensajeInfo("Estadísticas finales:");
        ui.mostrarMensajeInfo("- Total votos procesados: " + getTotalVotos());
        ui.mostrarMensajeInfo("- Total votantes: " + getTotalVotantes());
        
        ui.cerrar();
    }

    /**
     * Obtiene la UI para uso externo si es necesario.
     * 
     * @return Instancia de ServidorUI
     */
    public ServidorUI getUI() {
        return ui;
    }
}