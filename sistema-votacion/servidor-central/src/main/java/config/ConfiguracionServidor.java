package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Maneja la configuracion del servidor central de votacion cargada desde archivos de propiedades.
 * 
 * Esta clase es responsable de:
 * - Cargar configuracion desde archivos .properties
 * - Proporcionar acceso a parametros de configuracion del servidor central
 * - Mantener configuracion de conectividad y timeouts
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-06-15
 */
public class ConfiguracionServidor {
    
    private Properties properties;
    
    /**
     * Constructor que inicializa la configuracion desde el archivo especificado.
     * 
     * @param rutaArchivo Ruta al archivo de configuracion (.properties)
     */
    public ConfiguracionServidor(String rutaArchivo) {
        this.properties = new Properties();
        cargarConfiguracion(rutaArchivo);
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
    }    // Getters para propiedades del servidor central de votacion
    
    /**
     * Obtiene el identificador del servidor central de votacion.
     * 
     * @return ID del servidor central de votacion
     */
    public String getServidorId() {
        return properties.getProperty("nodo.id", "SERVIDOR-CENTRAL-DEFAULT");
    }    
    /**
     * Obtiene el nombre del servidor central de votacion.
     * 
     * @return Nombre del servidor central de votacion
     */
    public String getServidorNombre() {
        return properties.getProperty("nodo.nombre", "Servidor Central Default");
    }
    
    /**
     * Obtiene el host del servidor central de votacion.
     * 
     * @return Host donde esta ejecutandose el servidor central
     */
    public String getHost() {
        return properties.getProperty("servidor.host", "localhost");
    }
    
    /**
     * Obtiene el puerto del servidor central de votacion.
     * 
     * @return Puerto donde escucha el servidor central
     */
    public int getPuerto() {
        return Integer.parseInt(properties.getProperty("servidor.puerto", "6000"));
    }
    
    /**
     * Obtiene el timeout general del servidor central.
     * 
     * @return Timeout en milisegundos
     */
    public int getTimeout() {
        return Integer.parseInt(properties.getProperty("servidor.timeout", "5000"));
    }    
    /**
     * Obtiene el numero de reintentos para conexiones.
     * 
     * @return Numero de reintentos permitidos
     */
    public int getConexionReintentos() {
        return Integer.parseInt(properties.getProperty("conexion.reintentos", "3"));
    }
    
    /**
     * Obtiene el timeout para intentos de conexion.
     * 
     * @return Timeout de conexion en milisegundos
     */
    public int getConexionTimeout() {
        return Integer.parseInt(properties.getProperty("conexion.timeout", "3000"));
    }
}
