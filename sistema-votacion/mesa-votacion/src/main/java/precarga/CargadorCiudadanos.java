package precarga;

import model.Ciudadano;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cargador de ciudadanos desde archivo CSV para mesa de votación.
 * 
 * Responsabilidades:
 * - Leer archivo ciudadanos_mesa.csv
 * - Parsear datos de ciudadanos con validación
 * - Crear objetos Ciudadano con yaVoto=false por defecto
 * - Proporcionar acceso O(1) via HashMap por documento
 * 
 * Formato CSV esperado: id,documento,nombre,apellido,mesa_id
 * Ejemplo: 1,711674049,Fanny,Aguilar,1
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-06-16
 */
public class CargadorCiudadanos {
    
    private final String rutaArchivo;
    private Map<String, Ciudadano> ciudadanosPorDocumento;
    
    /**
     * Constructor del cargador de ciudadanos.
     * 
     * @param rutaArchivo Ruta al archivo ciudadanos_mesa.csv
     */
    public CargadorCiudadanos(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
        this.ciudadanosPorDocumento = new HashMap<>();
    }
    
    /**
     * Verifica si el archivo de ciudadanos existe.
     * 
     * @return true si el archivo existe y es legible
     */
    public boolean existeArchivoCiudadanos() {
        java.io.File archivo = new java.io.File(rutaArchivo);
        return archivo.exists() && archivo.isFile() && archivo.canRead();
    }
    
    /**
     * Carga todos los ciudadanos desde el archivo CSV.
     * Los ciudadanos se cargan con yaVoto=false por defecto.
     * 
     * @return Lista de ciudadanos cargados desde el CSV
     * @throws RuntimeException si hay error leyendo el archivo
     */
    public List<Ciudadano> cargarCiudadanos() {
        List<Ciudadano> ciudadanos = new ArrayList<>();
        ciudadanosPorDocumento.clear();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            int numeroLinea = 0;
            
            while ((linea = reader.readLine()) != null) {
                numeroLinea++;
                
                // Saltar header si existe
                if (numeroLinea == 1 && linea.toLowerCase().contains("documento")) {
                    continue;
                }
                
                // Saltar líneas vacías
                if (linea.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    Ciudadano ciudadano = parsearLineaCiudadano(linea, numeroLinea);
                    
                    // Verificar documento único
                    if (ciudadanosPorDocumento.containsKey(ciudadano.getDocumento())) {
                        System.err.println("ADVERTENCIA: Documento duplicado ignorado en línea " + 
                                         numeroLinea + ": " + ciudadano.getDocumento());
                        continue;
                    }
                    
                    ciudadanos.add(ciudadano);
                    ciudadanosPorDocumento.put(ciudadano.getDocumento(), ciudadano);
                    
                } catch (Exception e) {
                    System.err.println("Error procesando línea " + numeroLinea + ": " + e.getMessage());
                    // Continuar con la siguiente línea en lugar de fallar completamente
                }
            }
            
            System.out.println("[CARGA CSV] Ciudadanos cargados: " + ciudadanos.size() + " desde " + rutaArchivo);
            
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo archivo de ciudadanos: " + e.getMessage());
        }
        
        return ciudadanos;
    }
    
    /**
     * Parsea una línea CSV y crea un objeto Ciudadano.
     * 
     * Formato esperado: id,documento,nombre,apellido,mesa_id
     * 
     * @param linea Línea CSV a parsear
     * @param numeroLinea Número de línea para reportes de error
     * @return Ciudadano parseado
     * @throws IllegalArgumentException si la línea tiene formato inválido
     */
    private Ciudadano parsearLineaCiudadano(String linea, int numeroLinea) {
        String[] campos = linea.split(",");
        
        if (campos.length != 5) {
            throw new IllegalArgumentException("Formato CSV inválido. Se esperan 5 campos, encontrados: " + campos.length);
        }
        
        try {
            // Parsear campos
            Integer id = Integer.parseInt(campos[0].trim());
            String documento = campos[1].trim();
            String nombre = campos[2].trim();
            String apellido = campos[3].trim();
            String mesaId = campos[4].trim();
            
            // Validaciones básicas
            if (documento.isEmpty()) {
                throw new IllegalArgumentException("Documento no puede estar vacío");
            }
            
            if (nombre.isEmpty()) {
                throw new IllegalArgumentException("Nombre no puede estar vacío");
            }
            
            if (apellido.isEmpty()) {
                throw new IllegalArgumentException("Apellido no puede estar vacío");
            }
            
            // Crear ciudadano con yaVoto=false por defecto
            Ciudadano ciudadano = new Ciudadano(id, documento, nombre, apellido, mesaId);
            
            return ciudadano;
            
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Error parseando ID: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene el mapa de ciudadanos por documento para acceso O(1).
     * 
     * @return HashMap con documento como clave y Ciudadano como valor
     */
    public Map<String, Ciudadano> getCiudadanosPorDocumento() {
        return new HashMap<>(ciudadanosPorDocumento);
    }
    
    /**
     * Busca un ciudadano por documento (acceso O(1)).
     * 
     * @param documento Documento del ciudadano a buscar
     * @return Ciudadano encontrado o null si no existe
     */
    public Ciudadano buscarPorDocumento(String documento) {
        return ciudadanosPorDocumento.get(documento);
    }
    
    /**
     * Verifica si un ciudadano puede votar en esta mesa (acceso O(1)).
     * Solo verifica que el documento esté en el HashMap, no importa el mesa_id.
     * 
     * @param documento Documento del ciudadano
     * @return true si el ciudadano puede votar en esta mesa
     */
    public boolean puedeVotar(String documento) {
        Ciudadano ciudadano = ciudadanosPorDocumento.get(documento);
        return ciudadano != null && !ciudadano.isYaVoto();
    }
    
    /**
     * Obtiene estadísticas de carga.
     * 
     * @return Número total de ciudadanos cargados
     */
    public int getTotalCiudadanos() {
        return ciudadanosPorDocumento.size();
    }
}
