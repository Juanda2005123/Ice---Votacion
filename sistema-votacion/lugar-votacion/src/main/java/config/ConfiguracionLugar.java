package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Maneja la configuracion del lugar de votacion cargada desde archivos de propiedades.
 * 
 * Esta clase es responsable de:
 * - Cargar configuracion desde archivos .properties
 * - Proporcionar acceso a parametros de configuracion del lugar
 * - Gestionar informacion del broker destino para reenvio
 * - Mantener configuracion de conectividad y timeouts
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-06-14
 */
public class ConfiguracionLugar {
    
    private Properties properties;
    
    /**
     * Constructor que inicializa la configuracion desde el archivo especificado.
     * 
     * @param rutaArchivo Ruta al archivo de configuracion (.properties)
     */
    public ConfiguracionLugar(String rutaArchivo) {
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
    }
    
    // Getters para propiedades del lugar de votacion (actualizados para usar nodo.id)
    
    /**
     * Obtiene el identificador del lugar de votacion.
     * 
     * @return ID del lugar de votacion
     */
    public String getLugarId() {
        return properties.getProperty("nodo.id", "LUGAR-DEFAULT");
    }    
    /**
     * Obtiene el nombre del lugar de votacion.
     * 
     * @return Nombre del lugar de votacion
     */
    public String getLugarNombre() {
        return properties.getProperty("nodo.nombre", "Lugar Default");
    }
    
    /**
     * Obtiene el host del lugar de votacion.
     * 
     * @return Host donde esta ejecutandose el lugar
     */
    public String getHost() {
        return properties.getProperty("lugar.host", "localhost");
    }
    
    /**
     * Obtiene el puerto del lugar de votacion.
     * 
     * @return Puerto donde escucha el lugar
     */
    public int getPuerto() {
        return Integer.parseInt(properties.getProperty("lugar.puerto", "8001"));
    }
    
    /**
     * Obtiene el timeout general del lugar.
     * 
     * @return Timeout en milisegundos
     */
    public int getTimeout() {
        return Integer.parseInt(properties.getProperty("lugar.timeout", "5000"));
    }
    
    // Configuracion del broker destino (para reenvio)
    
    /**
     * Obtiene el host del broker destino para reenvio.
     * 
     * @return Host del broker lugar-departamento
     */
    public String getBrokerDestinoHost() {
        return properties.getProperty("broker.destino.host", "localhost");
    }
    
    /**
     * Obtiene el puerto del broker destino para reenvio.
     * 
     * @return Puerto del broker lugar-departamento
     */
    public int getBrokerDestinoPuerto() {
        return Integer.parseInt(properties.getProperty("broker.destino.puerto", "9001"));
    }
    
    /**
     * Obtiene el timeout especifico para comunicacion con broker destino.
     * 
     * @return Timeout para broker destino en milisegundos
     */
    public int getBrokerDestinoTimeout() {
        return Integer.parseInt(properties.getProperty("broker.destino.timeout", "5000"));
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
