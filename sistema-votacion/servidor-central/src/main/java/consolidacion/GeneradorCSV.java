package consolidacion;

import gestion.GestorCandidatos;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Generador de reportes CSV con los resultados nacionales finales.
 * 
 * Esta clase es responsable de:
 * - Generar archivo CSV con resultados del conteo nacional
 * - Formato: candidateId,candidateName,totalVotes
 * - Incluir nombres de candidatos desde el gestor de candidatos
 * - Sobreescribir archivo existente si existe
 * 
 * @author Sistema de Votacion
 * @version 4.0 - Con nombres de candidatos
 * @since 2025-06-16
 */
public class GeneradorCSV {
    
    private static final String NOMBRE_ARCHIVO = "resumen.csv";
    private static final String SEPARADOR = ",";
    
    private GestorCandidatos gestorCandidatos;
    
    /**
     * Constructor que inicializa el generador con el gestor de candidatos.
     * 
     * @param gestorCandidatos Gestor para obtener nombres de candidatos
     */
    public GeneradorCSV(GestorCandidatos gestorCandidatos) {
        this.gestorCandidatos = gestorCandidatos;
    }
      /**
     * Genera el archivo CSV con los resultados del conteo nacional incluyendo nombres.
     * 
     * @param conteoNacional Mapa con candidatoId -> totalVotos
     * @return true si el archivo se generó exitosamente, false en caso contrario
     */
    public boolean generarReporte(Map<Integer, Integer> conteoNacional) {
        try (FileWriter writer = new FileWriter(NOMBRE_ARCHIVO)) {
            // Escribir encabezado con nombres de candidatos
            writer.write("candidateId" + SEPARADOR + "candidateName" + SEPARADOR + "totalVotes\n");
            
            // Escribir datos ordenados por candidateId, incluyendo nombres
            conteoNacional.entrySet().stream()
                         .sorted(Map.Entry.comparingByKey())
                         .forEach(entry -> {
                             try {
                                 Integer candidatoId = entry.getKey();
                                 Integer totalVotos = entry.getValue();
                                 String nombreCandidato = gestorCandidatos.getNombreCandidato(candidatoId);
                                 
                                 // Escapar nombres que contengan comas
                                 if (nombreCandidato.contains(",")) {
                                     nombreCandidato = "\"" + nombreCandidato + "\"";
                                 }
                                 
                                 writer.write(candidatoId + SEPARADOR + nombreCandidato + SEPARADOR + totalVotos + "\n");
                             } catch (IOException e) {
                                 throw new RuntimeException("Error escribiendo línea CSV", e);
                             }
                         });
            
            System.out.println("Reporte CSV generado exitosamente: " + NOMBRE_ARCHIVO);
            System.out.println("Total de candidatos: " + conteoNacional.size());
            
            int totalVotos = conteoNacional.values().stream().mapToInt(Integer::intValue).sum();
            System.out.println("Total de votos nacionales: " + totalVotos);
            
            // Mostrar resumen por candidato
            System.out.println("\n=== RESUMEN FINAL DE VOTACIÓN ===");
            conteoNacional.entrySet().stream()
                         .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                         .forEach(entry -> {
                             Integer candidatoId = entry.getKey();
                             Integer votos = entry.getValue();
                             String nombre = gestorCandidatos.getNombreCandidato(candidatoId);
                             double porcentaje = totalVotos > 0 ? (votos * 100.0 / totalVotos) : 0.0;
                             System.out.printf("%s: %d votos (%.2f%%)%n", nombre, votos, porcentaje);
                         });
            
            return true;
            
        } catch (IOException e) {
            System.err.println("Error generando reporte CSV: " + e.getMessage());
            return false;
        }
    }
      /**
     * Genera un reporte CSV vacío si no hay datos.
     * 
     * @return true si el archivo se generó exitosamente
     */
    public boolean generarReporteVacio() {
        try (FileWriter writer = new FileWriter(NOMBRE_ARCHIVO)) {
            writer.write("candidateId" + SEPARADOR + "candidateName" + SEPARADOR + "totalVotes\n");
            System.out.println("Reporte CSV vacío generado: " + NOMBRE_ARCHIVO);
            return true;
        } catch (IOException e) {
            System.err.println("Error generando reporte CSV vacío: " + e.getMessage());
            return false;
        }
    }
}
