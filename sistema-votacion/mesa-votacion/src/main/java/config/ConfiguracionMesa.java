package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Maneja la configuración de la mesa de votación cargada desde mesa-votacion.properties
 */
public class ConfiguracionMesa {
    
    private Properties properties;
    
    public ConfiguracionMesa(String rutaArchivo) {
        this.properties = new Properties();
        cargarConfiguracion(rutaArchivo);
    }
    
    private void cargarConfiguracion(String rutaArchivo) {
        try (FileInputStream fis = new FileInputStream(rutaArchivo)) {
            properties.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Error cargando configuración de mesa: " + e.getMessage());
        }
    }
    
    // Getters para propiedades de la mesa
    public String getMesaId() {
        return properties.getProperty("mesa.id", "MESA-DEFAULT");
    }
    
    public String getMesaNombre() {
        return properties.getProperty("mesa.nombre", "Mesa Default");
    }
    
    // Getters para configuración del broker
    public String getBrokerHost() {
        return properties.getProperty("broker.host", "localhost");
    }
    
    public int getBrokerPuerto() {
        return Integer.parseInt(properties.getProperty("broker.puerto", "9000"));
    }
    
    public int getBrokerTimeout() {
        return Integer.parseInt(properties.getProperty("broker.timeout", "5000"));
    }
    
    // Getters para configuración de conexión
    public int getConexionReintentos() {
        return Integer.parseInt(properties.getProperty("conexion.reintentos", "3"));
    }
    
    public int getConexionTimeout() {
        return Integer.parseInt(properties.getProperty("conexion.timeout", "3000"));
    }
    
    // Getters para logging
    public String getLoggingNivel() {
        return properties.getProperty("logging.nivel", "INFO");
    }
    
    public String getLoggingArchivo() {
        return properties.getProperty("logging.archivo", "mesa-votacion.log");
    }
}
