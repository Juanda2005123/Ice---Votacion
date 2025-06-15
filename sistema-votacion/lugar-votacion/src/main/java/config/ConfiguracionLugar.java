package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Maneja la configuración del lugar de votación cargada desde lugar-votacion.properties
 */
public class ConfiguracionLugar {
    
    private Properties properties;
    
    public ConfiguracionLugar(String rutaArchivo) {
        this.properties = new Properties();
        cargarConfiguracion(rutaArchivo);
    }
    
    private void cargarConfiguracion(String rutaArchivo) {
        try (FileInputStream fis = new FileInputStream(rutaArchivo)) {
            properties.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Error cargando configuración: " + e.getMessage());
        }
    }
      // Getters para propiedades del lugar de votación (actualizados para usar nodo.id)
    public String getLugarId() {
        return properties.getProperty("nodo.id", "LUGAR-DEFAULT");
    }
    
    public String getLugarNombre() {
        return properties.getProperty("nodo.nombre", "Lugar Default");
    }
    
    public String getHost() {
        return properties.getProperty("lugar.host", "localhost");
    }
    
    public int getPuerto() {
        return Integer.parseInt(properties.getProperty("lugar.puerto", "8001"));
    }
    
    public int getTimeout() {
        return Integer.parseInt(properties.getProperty("lugar.timeout", "5000"));
    }
    
    // Configuración del broker destino (para reenvío)
    public String getBrokerDestinoHost() {
        return properties.getProperty("broker.destino.host", "localhost");
    }
    
    public int getBrokerDestinoPuerto() {
        return Integer.parseInt(properties.getProperty("broker.destino.puerto", "9001"));
    }
    
    public int getBrokerDestinoTimeout() {
        return Integer.parseInt(properties.getProperty("broker.destino.timeout", "5000"));
    }
    
    public int getConexionReintentos() {
        return Integer.parseInt(properties.getProperty("conexion.reintentos", "3"));
    }
    
    public int getConexionTimeout() {
        return Integer.parseInt(properties.getProperty("conexion.timeout", "3000"));
    }
}
