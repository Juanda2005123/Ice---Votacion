package votos;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.Candidato;
import model.Votante;
import model.Voto;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clase encargada de la persistencia de votos en archivos JSON.
 * Implementa el patrón Reliable Message manteniendo dos archivos:
 * 1. Archivo de auditoría: Contiene TODOS los votos para auditoría permanente
 * 2. Archivo de pendientes: Contiene votos que esperan confirmación del servidor
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-06-01
 */
public class PersistenciaVotos {
    
    private final Gson gson;
    private final String archivoAuditoria;
    private final String archivoPendientes;
    
    /**
     * Constructor que inicializa los archivos de persistencia.
     * 
     * @param idMesa ID de la mesa de votación para generar nombres únicos de archivo
     */
    public PersistenciaVotos(String idMesa) {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();
        
        this.archivoAuditoria = "auditoria_votos_mesa_" + idMesa + ".json";
        this.archivoPendientes = "pendientes_votos_mesa_" + idMesa + ".json";
    }
    
    /**
     * Guarda un voto completo en ambos archivos (auditoría y pendientes).
     * 
     * @param voto El voto a guardar
     * @param votante El votante que emitió el voto
     * @param candidato El candidato por quien se votó
     * @param timestamp Momento en que se registró el voto
     */
    public synchronized void guardarVotoCompleto(Voto voto, Votante votante, Candidato candidato, LocalDateTime timestamp) {
        EntradaVotoCompleta entrada = new EntradaVotoCompleta(voto, votante, candidato, timestamp);
        
        // Guardar en archivo de auditoría (permanente)
        guardarEnAuditoria(entrada);
        
        // Guardar en archivo de pendientes (temporal hasta confirmación)
        guardarEnPendientes(entrada);
    }
    
    /**
     * Guarda una entrada en el archivo de auditoría.
     * 
     * @param entrada La entrada a guardar
     */
    private void guardarEnAuditoria(EntradaVotoCompleta entrada) {
        List<EntradaVotoCompleta> entradas = cargarDeArchivo(archivoAuditoria);
        entradas.add(entrada);
        guardarEnArchivo(entradas, archivoAuditoria);
    }
    
    /**
     * Guarda una entrada en el archivo de pendientes.
     * 
     * @param entrada La entrada a guardar
     */
    private void guardarEnPendientes(EntradaVotoCompleta entrada) {
        List<EntradaVotoCompleta> entradas = cargarDeArchivo(archivoPendientes);
        entradas.add(entrada);
        guardarEnArchivo(entradas, archivoPendientes);
    }
      /**
     * Confirma que un voto fue enviado exitosamente al servidor.
     * Remueve el voto del archivo de pendientes pero lo mantiene en auditoría.
     * 
     * @param idVoto ID del voto confirmado
     * @return true si se encontró y removió el voto, false en caso contrario
     */
    public synchronized boolean confirmarVotoEnviado(String idVoto) {
        List<EntradaVotoCompleta> pendientes = cargarDeArchivo(archivoPendientes);
        boolean removido = pendientes.removeIf(entrada -> entrada.voto.getVotoId().equals(idVoto));
        if (removido) {
            guardarEnArchivo(pendientes, archivoPendientes);
        }
        return removido;
    }
    
    /**
     * Carga todos los mensajes pendientes de confirmación.
     * 
     * @return Lista de votos pendientes de confirmación
     */
    public synchronized List<EntradaVotoCompleta> cargarMensajesPendientes() {
        return cargarDeArchivo(archivoPendientes);
    }
    
    /**
     * Carga todo el historial de auditoría.
     * 
     * @return Lista completa de votos para auditoría
     */
    public synchronized List<EntradaVotoCompleta> cargarAuditoria() {
        return cargarDeArchivo(archivoAuditoria);
    }
    
    /**
     * Carga entradas desde un archivo JSON.
     * 
     * @param nombreArchivo Nombre del archivo a cargar
     * @return Lista de entradas cargadas o lista vacía si el archivo no existe
     */
    private List<EntradaVotoCompleta> cargarDeArchivo(String nombreArchivo) {
        File archivo = new File(nombreArchivo);
        if (!archivo.exists()) {
            return new ArrayList<>();
        }
        
        try (FileReader reader = new FileReader(archivo)) {
            Type tipoLista = new TypeToken<List<EntradaVotoCompleta>>(){}.getType();
            List<EntradaVotoCompleta> entradas = gson.fromJson(reader, tipoLista);
            return entradas != null ? entradas : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Error al cargar archivo " + nombreArchivo + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Guarda una lista de entradas en un archivo JSON.
     * 
     * @param entradas Lista de entradas a guardar
     * @param nombreArchivo Nombre del archivo donde guardar
     */
    private void guardarEnArchivo(List<EntradaVotoCompleta> entradas, String nombreArchivo) {
        try (FileWriter writer = new FileWriter(nombreArchivo)) {
            gson.toJson(entradas, writer);
        } catch (IOException e) {
            System.err.println("Error al guardar archivo " + nombreArchivo + ": " + e.getMessage());
        }
    }
      /**
     * Obtiene estadísticas de la persistencia.
     * 
     * @return Mapa con estadísticas de auditoría y pendientes
     */
    public Map<String, Integer> obtenerEstadisticas() {
        int totalAuditoria = cargarAuditoria().size();
        int totalPendientes = cargarMensajesPendientes().size();
        Map<String, Integer> stats = new HashMap<>();
        stats.put("auditoria", totalAuditoria);
        stats.put("pendientes", totalPendientes);
        return stats;
    }
    
    /**
     * Obtiene los nombres de los archivos de persistencia.
     * 
     * @return Mapa con nombres de archivos
     */
    public Map<String, String> obtenerNombresArchivos() {
        Map<String, String> archivos = new HashMap<>();
        archivos.put("auditoria", archivoAuditoria);
        archivos.put("pendientes", archivoPendientes);
        return archivos;
    }
    
    /**
     * Clase interna que representa una entrada completa de voto con toda la información necesaria.
     */
    public static class EntradaVotoCompleta {
        public final Voto voto;
        public final Votante votante;
        public final Candidato candidato;
        public final LocalDateTime timestamp;
        
        public EntradaVotoCompleta(Voto voto, Votante votante, Candidato candidato, LocalDateTime timestamp) {
            this.voto = voto;
            this.votante = votante;
            this.candidato = candidato;
            this.timestamp = timestamp;
        }
          @Override
        public String toString() {
            return String.format("Voto[%s] - Votante[%s] - Candidato[%s] - %s", 
                    voto.getVotoId(), votante.getNombre(), candidato.getNombre(), timestamp);
        }
    }
}