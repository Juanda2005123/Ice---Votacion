package precarga;

import model.Candidato;
import model.Ciudadano;
import votos.RepositorioMesaVotacion;
import java.util.ArrayList;
import java.util.List;

/**
 * Sistema de precarga de datos para mesa de votacion.
 * Se encarga de procesar y validar datos de configuracion antes de cargarlos.
 * 
 * Responsabilidades:
 * - Procesar configuracion recibida del servidor central
 * - Validar integridad de datos de precarga
 * - Cargar datos en el repositorio de manera controlada
 * - Manejar errores de precarga y rollback
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-05-30
 */
public class SistemaPrecarga {
    private RepositorioMesaVotacion repositorio;
    private boolean precargaCompletada;
    private CargadorCandidatos cargadorCandidatos;
    private CargadorCiudadanos cargadorCiudadanos;      /**
     * Constructor del sistema de precarga.
     * 
     * @param repositorio Repositorio donde cargar los datos
     * @param rutaCandidatos Ruta al archivo CSV de candidatos
     */
    public SistemaPrecarga(RepositorioMesaVotacion repositorio, String rutaCandidatos) {
        if (repositorio == null) {
            throw new IllegalArgumentException("El repositorio no puede ser null");
        }
        
        this.repositorio = repositorio;
        this.precargaCompletada = false;
        this.cargadorCandidatos = new CargadorCandidatos(rutaCandidatos);
        
        // Determinar ruta del archivo de ciudadanos (mismo directorio que candidatos)
        String directorioConfig = obtenerDirectorio(rutaCandidatos);
        String rutaCiudadanos = directorioConfig + "ciudadanos_mesa.csv";
        this.cargadorCiudadanos = new CargadorCiudadanos(rutaCiudadanos);
        
        // Verificar que ambos archivos existen
        if (!cargadorCandidatos.existeArchivoCandidatos()) {
            throw new RuntimeException("Archivo de candidatos no encontrado: " + rutaCandidatos);
        }
        
        if (!cargadorCiudadanos.existeArchivoCiudadanos()) {
            throw new RuntimeException("Archivo de ciudadanos no encontrado: " + rutaCiudadanos);
        }
    }
      /**
     * Realiza la precarga completa de la mesa cargando candidatos desde CSV
     * y generando votantes simulados.
     * 
     * @throws RuntimeException si hay error en la precarga
     */
    public void precargarMesa() {
        if (precargaCompletada) {
            throw new RuntimeException("La mesa ya ha sido precargada. No se permite recargar.");
        }
          try {
            // 1. Cargar candidatos desde CSV
            List<Candidato> candidatos = cargadorCandidatos.cargarCandidatos();
            precargarCandidatos(candidatos);
            
            // 2. Cargar ciudadanos desde CSV
            List<Ciudadano> ciudadanos = cargadorCiudadanos.cargarCiudadanos();
            precargarCiudadanos(ciudadanos);
            
            // 3. Marcar precarga como completada
            this.precargaCompletada = true;
            
            System.out.println("[PRECARGA] Mesa precargada exitosamente: " + repositorio.getIdMesaVotacion());
            System.out.println("[PRECARGA] Candidatos: " + candidatos.size() + ", Ciudadanos: " + ciudadanos.size());
            
        } catch (Exception e) {
            throw new RuntimeException("Error durante la precarga: " + e.getMessage());
        }
    }
    
    /**
     * Precarga la lista de candidatos con validaciones adicionales.
     * 
     * @param candidatos Lista de candidatos a precargar
     * @throws IllegalArgumentException si hay candidatos invalidos
     */
    private void precargarCandidatos(List<Candidato> candidatos) {
        // Validaciones adicionales especificas para candidatos
        List<Integer> idsUnicos = new ArrayList<>();
        
        for (Candidato candidato : candidatos) {
            if (candidato.getId() == null) {
                throw new IllegalArgumentException("Candidato con ID invalido encontrado");
            }
            
            if (candidato.getNombre() == null || candidato.getNombre().trim().isEmpty()) {
                throw new IllegalArgumentException("Candidato sin nombre encontrado: " + candidato.getId());
            }
            
            // Verificar IDs unicos
            if (idsUnicos.contains(candidato.getId())) {
                throw new IllegalArgumentException("ID de candidato duplicado: " + candidato.getId());
            }
            idsUnicos.add(candidato.getId());
        }
        
        // Verificar que exista al menos un candidato normal
        if (candidatos.isEmpty()) {
            throw new IllegalArgumentException("Debe existir al menos un candidato");
        }
        
        // Cargar en repositorio
        repositorio.cargarCandidatos(candidatos);
    }
      /**
     * Precarga la lista de ciudadanos con validaciones adicionales.
     * 
     * @param ciudadanos Lista de ciudadanos a precargar
     * @throws IllegalArgumentException si hay ciudadanos invalidos
     */
    private void precargarCiudadanos(List<Ciudadano> ciudadanos) {
        // Validaciones adicionales especificas para ciudadanos
        List<String> documentosUnicos = new ArrayList<>();
        
        for (Ciudadano ciudadano : ciudadanos) {
            if (ciudadano.getDocumento() == null || ciudadano.getDocumento().trim().isEmpty()) {
                throw new IllegalArgumentException("Ciudadano con documento invalido encontrado");
            }
            
            if (ciudadano.getNombre() == null || ciudadano.getNombre().trim().isEmpty()) {
                throw new IllegalArgumentException("Ciudadano sin nombre encontrado: " + ciudadano.getDocumento());
            }
            
            if (ciudadano.getApellido() == null || ciudadano.getApellido().trim().isEmpty()) {
                throw new IllegalArgumentException("Ciudadano sin apellido encontrado: " + ciudadano.getDocumento());
            }
            
            // Verificar documentos unicos
            if (documentosUnicos.contains(ciudadano.getDocumento())) {
                throw new IllegalArgumentException("Documento duplicado encontrado: " + ciudadano.getDocumento());
            }
            documentosUnicos.add(ciudadano.getDocumento());
            
            // IMPORTANTE: No validamos mesa_id - si está en el CSV, puede votar en esta mesa
        }
        
        // Verificar que hay ciudadanos para cargar
        if (ciudadanos.isEmpty()) {
            throw new IllegalArgumentException("Debe existir al menos un ciudadano en el archivo CSV");
        }
        
        // Cargar en repositorio
        repositorio.cargarVotantesElegibles(ciudadanos);
    }      /**
     * Obtiene el directorio de un archivo dado su ruta completa.
     * 
     * @param rutaArchivo Ruta completa del archivo
     * @return Directorio del archivo
     */
    private String obtenerDirectorio(String rutaArchivo) {
        java.io.File archivo = new java.io.File(rutaArchivo);
        String directorio = archivo.getParent();
        
        if (directorio == null) {
            return "./"; // Directorio actual si no hay parent
        }
        
        // Asegurar que termine con separador de archivo
        if (!directorio.endsWith(java.io.File.separator)) {
            directorio += java.io.File.separator;
        }
        
        return directorio;
    }
    
    /**
     * Verifica si la precarga ha sido completada.
     * 
     * @return true si la mesa esta precargada
     */
    public boolean isPrecargaCompletada() {
        return precargaCompletada;
    }
    
    /**
     * Obtiene el repositorio asociado.
     * 
     * @return Repositorio de mesa de votacion
     */
    public RepositorioMesaVotacion getRepositorio() {
        return repositorio;
    }
    
    /**    /**
     * Obtiene el cargador de ciudadanos para acceso directo O(1).
     * 
     * @return Cargador de ciudadanos con HashMap interno
     */
    public CargadorCiudadanos getCargadorCiudadanos() {
        return cargadorCiudadanos;
    }
}