package precarga;

import model.Candidato;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Cargador de candidatos desde archivo CSV para mesa de votación.
 * 
 * Esta clase es responsable de:
 * - Cargar la información de candidatos desde archivo CSV externo
 * - Proporcionar lista de candidatos para precarga de mesa
 * - Validar formato y consistencia de datos de candidatos
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-06-16
 */
public class CargadorCandidatos {
    
    private String rutaArchivoCandidatos;
    
    /**
     * Constructor que inicializa el cargador con la ruta del archivo de candidatos.
     * 
     * @param rutaArchivoCandidatos Ruta al archivo CSV de candidatos
     */
    public CargadorCandidatos(String rutaArchivoCandidatos) {
        this.rutaArchivoCandidatos = rutaArchivoCandidatos;
    }
    
    /**
     * Carga los candidatos desde el archivo CSV.
     * El formato esperado es: id,nombre,partido_politico
     * 
     * @return Lista de candidatos cargados desde el CSV
     * @throws RuntimeException si hay error al cargar el archivo
     */
    public List<Candidato> cargarCandidatos() {
        List<Candidato> candidatos = new ArrayList<>();
        
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
                        candidatos.add(candidato);
                        
                        System.out.printf("[MESA-CANDIDATOS] Cargado: ID=%d, Nombre='%s', Partido='%s'%n", 
                                        id, nombre, partidoPolitico);
                    } else {
                        System.err.printf("[MESA-CANDIDATOS] Línea %d inválida (faltan campos): %s%n", 
                                        numeroLinea, linea);
                    }
                } catch (NumberFormatException e) {
                    System.err.printf("[MESA-CANDIDATOS] Error en línea %d (ID inválido): %s%n", 
                                    numeroLinea, linea);
                }
            }
            
            System.out.printf("[MESA-CANDIDATOS] Cargados %d candidatos desde: %s%n", 
                            candidatos.size(), rutaArchivoCandidatos);
            
        } catch (IOException e) {
            throw new RuntimeException("Error cargando candidatos desde: " + rutaArchivoCandidatos + 
                                     " - " + e.getMessage());
        }
        
        // Validar que se cargaron candidatos
        if (candidatos.isEmpty()) {
            throw new RuntimeException("No se pudieron cargar candidatos desde: " + rutaArchivoCandidatos);
        }
        
        return candidatos;
    }
    
    /**
     * Verifica si el archivo de candidatos existe.
     * 
     * @return true si el archivo existe, false en caso contrario
     */
    public boolean existeArchivoCandidatos() {
        java.io.File archivo = new java.io.File(rutaArchivoCandidatos);
        return archivo.exists() && archivo.canRead();
    }
}
