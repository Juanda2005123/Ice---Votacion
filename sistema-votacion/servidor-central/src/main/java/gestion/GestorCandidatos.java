package gestion;

import model.Candidato;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Gestor de candidatos del sistema de votación.
 * 
 * Esta clase es responsable de:
 * - Cargar la información de candidatos desde archivo CSV
 * - Mantener un registro en memoria de todos los candidatos
 * - Proporcionar acceso rápido a información de candidatos por ID
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-06-16
 */
public class GestorCandidatos {
    
    private Map<Integer, Candidato> candidatos;
    private String rutaArchivoCandidatos;
    
    /**
     * Constructor que inicializa el gestor con la ruta del archivo de candidatos.
     * 
     * @param rutaArchivoCandidatos Ruta al archivo CSV de candidatos
     */
    public GestorCandidatos(String rutaArchivoCandidatos) {
        this.rutaArchivoCandidatos = rutaArchivoCandidatos;
        this.candidatos = new HashMap<>();
        cargarCandidatos();
    }
    
    /**
     * Carga los candidatos desde el archivo CSV.
     * El formato esperado es: id,nombre,partido_politico
     */
    private void cargarCandidatos() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(rutaArchivoCandidatos), "UTF-8"))) {
            
            String linea;
            int numeroLinea = 0;
            
            while ((linea = reader.readLine()) != null) {
                numeroLinea++;
                
                // Saltar líneas vacías y comentarios
                if (linea.trim().isEmpty() || linea.trim().startsWith("#")) {
                    continue;
                }
                
                try {
                    String[] campos = linea.split(",");
                    if (campos.length >= 3) {
                        int id = Integer.parseInt(campos[0].trim());
                        String nombre = campos[1].trim();
                        String partidoPolitico = campos[2].trim();
                        
                        Candidato candidato = new Candidato(id, nombre, partidoPolitico);
                        candidatos.put(id, candidato);
                        
                        System.out.printf("[CANDIDATOS] Cargado: ID=%d, Nombre='%s', Partido='%s'%n", 
                                        id, nombre, partidoPolitico);
                    } else {
                        System.err.printf("[CANDIDATOS] Línea %d inválida (faltan campos): %s%n", 
                                        numeroLinea, linea);
                    }
                } catch (NumberFormatException e) {
                    System.err.printf("[CANDIDATOS] Error en línea %d (ID inválido): %s%n", 
                                    numeroLinea, linea);
                }
            }
            
            System.out.printf("[CANDIDATOS] Cargados %d candidatos exitosamente%n", candidatos.size());
            
        } catch (IOException e) {
            throw new RuntimeException("Error cargando candidatos desde: " + rutaArchivoCandidatos + 
                                     " - " + e.getMessage());
        }
    }
    
    /**
     * Obtiene un candidato por su ID.
     * 
     * @param id ID del candidato
     * @return Candidato encontrado o null si no existe
     */
    public Candidato getCandidatoPorId(Integer id) {
        return candidatos.get(id);
    }
    
    /**
     * Obtiene el nombre de un candidato por su ID.
     * 
     * @param id ID del candidato
     * @return Nombre del candidato o "CANDIDATO DESCONOCIDO" si no existe
     */
    public String getNombreCandidato(Integer id) {
        Candidato candidato = candidatos.get(id);
        return candidato != null ? candidato.getNombre() : "CANDIDATO DESCONOCIDO (ID: " + id + ")";
    }
    
    /**
     * Obtiene todos los candidatos cargados.
     * 
     * @return Lista con todos los candidatos
     */
    public List<Candidato> getTodosCandidatos() {
        return new ArrayList<>(candidatos.values());
    }
    
    /**
     * Verifica si existe un candidato con el ID especificado.
     * 
     * @param id ID del candidato
     * @return true si existe, false en caso contrario
     */
    public boolean existeCandidato(Integer id) {
        return candidatos.containsKey(id);
    }
    
    /**
     * Obtiene el número total de candidatos cargados.
     * 
     * @return Número de candidatos
     */
    public int getNumCandidatos() {
        return candidatos.size();
    }
    
    /**
     * Obtiene información resumida de todos los candidatos para logging.
     * 
     * @return String con resumen de candidatos
     */
    public String getResumenCandidatos() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== CANDIDATOS CARGADOS ===\n");
        
        candidatos.entrySet().stream()
                .sorted(Map.Entry.<Integer, Candidato>comparingByKey())
                .forEach(entry -> {
                    Candidato c = entry.getValue();
                    sb.append(String.format("ID=%d | %s (%s)\n", 
                            c.getId(), c.getNombre(), c.getPartidoPolitico()));
                });
        
        sb.append(String.format("Total: %d candidatos", candidatos.size()));
        return sb.toString();
    }
}
