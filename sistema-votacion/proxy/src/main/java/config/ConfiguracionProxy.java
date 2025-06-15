package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Maneja la configuracion del proxy de validacion cargada desde archivos de propiedades.
 * 
 * Esta clase es responsable de:
 * - Cargar configuracion desde archivos .properties
 * - Proporcionar acceso a parametros de configuracion del proxy
 * - Gestionar informacion del nodo destino para reenvio de validaciones
 * - Mantener configuracion de conectividad y timeouts
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-06-15
 */
public class ConfiguracionProxy {
    
    private Properties properties;
    
    /**
     * Constructor que inicializa la configuracion desde el archivo especificado.
     * 
     * @param rutaArchivo Ruta al archivo de configuracion (.properties)
     */
    public ConfiguracionProxy(String rutaArchivo) {
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
    
    // Getters para propiedades del proxy de validacion
    
    /**
     * Obtiene el identificador del proxy de validacion.
     * 
     * @return ID del proxy
     */
    public String getProxyId() {
        return properties.getProperty("nodo.id", "PROXY-DEFAULT");
    }    
    /**
     * Obtiene el nombre del proxy de validacion.
     * 
     * @return Nombre del proxy
     */
    public String getProxyNombre() {
        return properties.getProperty("nodo.nombre", "Proxy Default");
    }
    
    /**
     * Obtiene el host del proxy de validacion.
     * 
     * @return Host donde esta ejecutandose el proxy
     */
    public String getHost() {
        return properties.getProperty("proxy.host", "localhost");
    }
    
    /**
     * Obtiene el puerto del proxy de validacion.
     * 
     * @return Puerto donde escucha el proxy
     */
    public int getPuerto() {
        return Integer.parseInt(properties.getProperty("proxy.puerto", "8001"));
    }
    
    /**
     * Obtiene el timeout general del proxy.
     * 
     * @return Timeout en milisegundos
     */
    public int getTimeout() {
        return Integer.parseInt(properties.getProperty("proxy.timeout", "5000"));
    }    
    // Configuracion del nodo destino (donde se reenvian las validaciones)
    
    /**
     * Obtiene el host del nodo destino para reenvio de validaciones.
     * 
     * @return Host del nodo destino
     */
    public String getNodoDestinoHost() {
        return properties.getProperty("nodo.destino.host", "localhost");
    }
    
    /**
     * Obtiene el puerto del nodo destino para reenvio de validaciones.
     * 
     * @return Puerto del nodo destino
     */
    public int getNodoDestinoPuerto() {
        return Integer.parseInt(properties.getProperty("nodo.destino.puerto", "9001"));
    }
    
    /**
     * Obtiene el timeout especifico para comunicacion con nodo destino.
     * 
     * @return Timeout para nodo destino en milisegundos
     */
    public int getNodoDestinoTimeout() {
        return Integer.parseInt(properties.getProperty("nodo.destino.timeout", "5000"));
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
