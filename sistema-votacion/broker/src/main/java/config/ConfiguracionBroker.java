package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Maneja la configuracion del broker cargada desde archivos de propiedades.
 * 
 * Esta clase es responsable de:
 * - Cargar configuracion desde archivos .properties
 * - Proporcionar acceso a parametros de configuracion del broker
 * - Gestionar la lista de destinos disponibles para enrutamiento
 * - Mantener el estado activo/inactivo de cada destino
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-06-14
 */
public class ConfiguracionBroker {
    
    private Properties properties;
    private List<Destino> destinos;
    
    /**
     * Constructor que inicializa la configuracion desde el archivo especificado.
     * 
     * @param rutaArchivo Ruta al archivo de configuracion (.properties)
     */
    public ConfiguracionBroker(String rutaArchivo) {
        this.properties = new Properties();
        this.destinos = new ArrayList<>();
        cargarConfiguracion(rutaArchivo);
        cargarDestinos();
    }
    
    /**
     * Carga la configuracion desde el archivo de propiedades.
     * 
     * @param rutaArchivo Ruta al archivo de configuracion
     */
    private void cargarConfiguracion(String rutaArchivo) {
        try (FileInputStream fis = new FileInputStream(rutaArchivo)) {
            properties.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Error cargando configuracion: " + e.getMessage());
        }
    }
      /**
     * Carga los destinos configurados desde las propiedades.
     */
    private void cargarDestinos() {
        int cantidad = Integer.parseInt(properties.getProperty("destinos.cantidad", "0"));
        
        for (int i = 1; i <= cantidad; i++) {
            String id = properties.getProperty("destino." + i + ".id");
            String host = properties.getProperty("destino." + i + ".host");
            String puertoStr = properties.getProperty("destino." + i + ".puerto");
            String activoStr = properties.getProperty("destino." + i + ".activo", "true");
            String tipo = properties.getProperty("destino." + i + ".tipo", "lugar-votacion");
            
            if (id != null && host != null && puertoStr != null) {
                int puerto = Integer.parseInt(puertoStr);
                boolean activo = Boolean.parseBoolean(activoStr);
                destinos.add(new Destino(id, host, puerto, activo, tipo));
            }
        }
    }
    
    // Getters para propiedades del broker (actualizados para usar nodo.id)
    
    /**
     * Obtiene el identificador del broker.
     * 
     * @return ID del broker configurado
     */
    public String getBrokerId() {
        return properties.getProperty("nodo.id", "BROKER-DEFAULT");
    }
    
    /**
     * Obtiene el nombre del broker.
     * 
     * @return Nombre del broker configurado
     */
    public String getBrokerNombre() {
        return properties.getProperty("nodo.nombre", "Broker Default");
    }
    
    /**
     * Obtiene el host del broker.
     * 
     * @return Host donde esta ejecutandose el broker
     */
    public String getHost() {
        return properties.getProperty("broker.host", "localhost");
    }
    
    /**
     * Obtiene el puerto del broker.
     * 
     * @return Puerto donde escucha el broker
     */
    public int getPuerto() {
        return Integer.parseInt(properties.getProperty("broker.puerto", "9000"));
    }
    
    /**
     * Obtiene el timeout general del broker.
     * 
     * @return Timeout en milisegundos
     */
    public int getTimeout() {
        return Integer.parseInt(properties.getProperty("broker.timeout", "5000"));
    }
    
    /**
     * Obtiene el timeout especifico para enrutamiento.
     * 
     * @return Timeout de enrutamiento en milisegundos
     */
    public int getEnrutamientoTimeout() {
        return Integer.parseInt(properties.getProperty("enrutamiento.timeout", "3000"));
    }
    
    /**
     * Obtiene la estrategia de enrutamiento configurada.
     * 
     * @return Estrategia de enrutamiento (ej: ALEATORIO)
     */
    public String getEstrategiaEnrutamiento() {
        return properties.getProperty("enrutamiento.estrategia", "ALEATORIO");
    }
    
    // Metodos para manejar destinos
    
    /**
     * Obtiene todos los destinos configurados.
     * 
     * @return Lista con todos los destinos (activos e inactivos)
     */
    public List<Destino> getTodosLosDestinos() {
        return new ArrayList<>(destinos);
    }
    
    /**
     * Obtiene solo los destinos activos.
     * 
     * @return Lista con destinos activos disponibles para enrutamiento
     */
    public List<Destino> getDestinosActivos() {
        return destinos.stream()
                .filter(Destino::isActivo)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    /**
     * Busca un destino por su identificador.
     * 
     * @param id Identificador del destino a buscar
     * @return Destino encontrado o null si no existe
     */
    public Destino getDestinoPorId(String id) {
        return destinos.stream()
                .filter(d -> d.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Clase interna que representa un destino de reenvio.
     * 
     * Encapsula la informacion necesaria para conectarse a un destino
     * de reenvio de votos (lugar de votacion, departamento, etc).
     * 
     * @author Sistema de Votacion
     * @version 1.0
     * @since 2025-06-14
     */
    public static class Destino {
        private String id;
        private String host;
        private int puerto;
        private boolean activo;
        private String tipo;
        
        /**
         * Constructor que inicializa un destino con todos sus parametros.
         * 
         * @param id Identificador unico del destino
         * @param host Host o direccion IP del destino
         * @param puerto Puerto de comunicacion del destino
         * @param activo Estado inicial del destino (activo/inactivo)
         * @param tipo Tipo de destino (lugar-votacion, departamento, etc)
         */
        public Destino(String id, String host, int puerto, boolean activo, String tipo) {
            this.id = id;
            this.host = host;            this.puerto = puerto;
            this.activo = activo;
            this.tipo = tipo;
        }
        
        // Getters
        
        /**
         * Obtiene el identificador del destino.
         * 
         * @return ID del destino
         */
        public String getId() { return id; }
        
        /**
         * Obtiene el host del destino.
         * 
         * @return Host o direccion IP del destino
         */
        public String getHost() { return host; }
        
        /**
         * Obtiene el puerto del destino.
         * 
         * @return Puerto de comunicacion del destino
         */
        public int getPuerto() { return puerto; }
        
        /**
         * Verifica si el destino esta activo.
         * 
         * @return true si el destino esta activo, false en caso contrario
         */
        public boolean isActivo() { return activo; }
        
        /**
         * Obtiene el tipo del destino.
         * 
         * @return Tipo del destino (lugar-votacion, departamento, etc)
         */
        public String getTipo() { return tipo; }
        
        // Setters
        
        /**
         * Establece el estado activo/inactivo del destino.
         * 
         * @param activo Nuevo estado del destino
         */
        public void setActivo(boolean activo) { this.activo = activo; }
        
        /**
         * Representacion en cadena del destino para debugging.
         * 
         * @return Cadena con informacion del destino
         */
        @Override
        public String toString() {
            return String.format("Destino{id='%s', host='%s', puerto=%d, activo=%s, tipo='%s'}", 
                               id, host, puerto, activo, tipo);
        }
    }
}
