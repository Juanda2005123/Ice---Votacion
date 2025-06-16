package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Maneja la configuración de la mesa de votación cargada desde mesa-votacion.properties
 * 
 * Esta clase reemplaza a ConfiguracionMesa y proporciona acceso robusto a todas las
 * propiedades de configuración con parsing seguro (usando .trim() en valores numéricos).
 * 
 * @author Sistema de Votacion
 * @version 2.0
 * @since 2025-06-16
 */
public class ConfiguracionMesaVotacion {
    
    private Properties properties;
    
    /**
     * Constructor para cargar configuración desde archivo.
     * 
     * @param rutaArchivo Ruta al archivo de configuración .properties
     */
    public ConfiguracionMesaVotacion(String rutaArchivo) {
        this.properties = new Properties();
        cargarConfiguracion(rutaArchivo);
    }
    
    private void cargarConfiguracion(String rutaArchivo) {
        try (FileInputStream fis = new FileInputStream(rutaArchivo)) {
            properties.load(fis);
            System.out.printf("[MESA-CONFIG] Configuración cargada desde: %s%n", rutaArchivo);
        } catch (IOException e) {
            throw new RuntimeException("Error cargando configuración de mesa: " + e.getMessage());
        }
    }
    
    // ===== PROPIEDADES DE LA MESA =====
    
    /**
     * Obtiene el ID único de la mesa de votación.
     * 
     * @return ID de la mesa (ej: "MESA-001")
     */
    public String getMesaId() {
        return properties.getProperty("node.id", "MESA-DEFAULT");
    }
    
    /**
     * Obtiene el nombre descriptivo de la mesa.
     * 
     * @return Nombre de la mesa
     */
    public String getMesaNombre() {
        return properties.getProperty("node.nombre", "Mesa Default");
    }
    
    // ===== CONFIGURACIÓN DEL BROKER =====
    
    /**
     * Obtiene el host del broker ICE.
     * 
     * @return Host del broker
     */
    public String getBrokerHost() {
        return properties.getProperty("broker.host", "localhost");
    }
    
    /**
     * Obtiene el puerto del broker ICE.
     * 
     * @return Puerto del broker
     */
    public int getBrokerPuerto() {
        return Integer.parseInt(properties.getProperty("broker.puerto", "9000").trim());
    }
    
    /**
     * Obtiene el timeout de conexión al broker.
     * 
     * @return Timeout en millisegundos
     */
    public int getBrokerTimeout() {
        return Integer.parseInt(properties.getProperty("broker.timeout", "5000").trim());
    }
    
    // ===== CONFIGURACIÓN DE CONEXIÓN =====
    
    /**
     * Obtiene el número de reintentos de conexión.
     * 
     * @return Número de reintentos
     */
    public int getConexionReintentos() {
        return Integer.parseInt(properties.getProperty("conexion.reintentos", "3").trim());
    }
    
    /**
     * Obtiene el timeout de conexión general.
     * 
     * @return Timeout en millisegundos
     */
    public int getConexionTimeout() {
        return Integer.parseInt(properties.getProperty("conexion.timeout", "3000").trim());
    }
    
    // ===== CONFIGURACIÓN DE LOGGING =====
    
    /**
     * Obtiene el nivel de logging.
     * 
     * @return Nivel de logging (INFO, DEBUG, ERROR, etc.)
     */
    public String getLoggingNivel() {
        return properties.getProperty("logging.nivel", "INFO");
    }
    
    /**
     * Obtiene el archivo de logging.
     * 
     * @return Ruta del archivo de log
     */
    public String getLoggingArchivo() {
        return properties.getProperty("logging.archivo", "mesa-votacion.log");
    }
    
    // ===== CONFIGURACIÓN DE DELTAS =====
    
    /**
     * Obtiene el umbral de votos para generar deltas.
     * 
     * @return Número de votos que activa envío de delta
     */
    public int getDeltaUmbralVotos() {
        return Integer.parseInt(properties.getProperty("delta.umbral.votos", "50").trim());
    }
    
    /**
     * Obtiene el umbral de tiempo para generar deltas.
     * 
     * @return Tiempo en segundos para forzar envío de delta
     */
    public long getDeltaUmbralTiempo() {
        return Long.parseLong(properties.getProperty("delta.umbral.tiempo", "300").trim());
    }
    
    // ===== CONFIGURACIÓN DE THREADS =====
    
    /**
     * Obtiene el tamaño del pool de threads para deltas.
     * 
     * @return Número de threads en el pool
     */
    public int getThreadsPoolSize() {
        return Integer.parseInt(properties.getProperty("threads.pool.size", "5").trim());
    }
}
