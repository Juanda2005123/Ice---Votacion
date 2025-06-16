package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Maneja la configuracion del departamento de votacion cargada desde archivos de propiedades.
 * 
 * Esta clase es responsable de:
 * - Cargar configuracion desde archivos .properties
 * - Proporcionar acceso a parametros de configuracion del departamento
 * - Gestionar informacion del broker destino para reenvio (servidor central)
 * - Mantener configuracion de conectividad y timeouts
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-06-15
 */
public class ConfiguracionDepartamento {
    
    private Properties properties;
      /**
     * Constructor que inicializa la configuracion desde el archivo especificado.
     * 
     * @param rutaArchivo Ruta al archivo de configuracion (.properties)
     */
    public ConfiguracionDepartamento(String rutaArchivo) {
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
      // Getters para propiedades del departamento de votacion (actualizados para usar nodo.id)
    
    /**
     * Obtiene el identificador del departamento de votacion.
     * 
     * @return ID del departamento de votacion
     */
    public String getDepartamentoId() {
        return properties.getProperty("nodo.id", "DEPARTAMENTO-DEFAULT");
    }    
    /**
     * Obtiene el nombre del departamento de votacion.
     * 
     * @return Nombre del departamento de votacion
     */
    public String getDepartamentoNombre() {
        return properties.getProperty("nodo.nombre", "Departamento Default");
    }
    
    /**
     * Obtiene el host del departamento de votacion.
     * 
     * @return Host donde esta ejecutandose el departamento
     */
    public String getHost() {
        return properties.getProperty("departamento.host", "localhost");
    }
      /**
     * Obtiene el puerto del departamento de votacion.
     * 
     * @return Puerto donde escucha el departamento
     */
    public int getPuerto() {
        return Integer.parseInt(properties.getProperty("departamento.puerto", "7001").trim());
    }
      /**
     * Obtiene el timeout general del departamento.
     * 
     * @return Timeout en milisegundos
     */
    public int getTimeout() {
        return Integer.parseInt(properties.getProperty("departamento.timeout", "5000").trim());
    }
      // Configuracion del servidor central (conexion directa - sin broker)
    
    /**
     * Obtiene el host del servidor central para conexion directa.
     * 
     * @return Host del servidor central
     */
    public String getServidorCentralHost() {
        return properties.getProperty("servidor.central.host", "localhost");
    }
      /**
     * Obtiene el puerto del servidor central para conexion directa.
     * 
     * @return Puerto del servidor central
     */
    public int getServidorCentralPuerto() {
        return Integer.parseInt(properties.getProperty("servidor.central.puerto", "6000").trim());
    }
      /**
     * Obtiene el timeout especifico para comunicacion con servidor central.
     * 
     * @return Timeout para servidor central en milisegundos
     */
    public int getServidorCentralTimeout() {
        return Integer.parseInt(properties.getProperty("servidor.central.timeout", "5000").trim());
    }
      /**
     * Obtiene el numero de reintentos para conexiones.
     * 
     * @return Numero de reintentos permitidos
     */
    public int getConexionReintentos() {
        return Integer.parseInt(properties.getProperty("conexion.reintentos", "3").trim());
    }
      /**
     * Obtiene el timeout para intentos de conexion.
     * 
     * @return Timeout de conexion en milisegundos
     */
    public int getConexionTimeout() {
        return Integer.parseInt(properties.getProperty("conexion.timeout", "3000").trim());
    }
    
    // Configuración de deltas Map-Reduce Level 2
      /**
     * Obtiene el umbral de votos para envío de deltas consolidadas departamentales.
     * 
     * @return Número de votos consolidados antes de enviar al servidor central
     */
    public int getDeltaUmbralVotos() {
        return Integer.parseInt(properties.getProperty("delta.umbral.votos", "1000").trim());
    }
      /**
     * Obtiene el umbral de tiempo para envío de deltas consolidadas departamentales.
     * 
     * @return Tiempo en milisegundos antes de enviar al servidor central
     */
    public long getDeltaUmbralTiempo() {
        return Long.parseLong(properties.getProperty("delta.umbral.tiempo", "2000").trim());
    }
      /**
     * Obtiene el tamaño del pool de hilos para Map-Reduce departamental.
     * 
     * @return Tamaño del pool de hilos para consolidación
     */
    public int getThreadPoolSize() {
        return Integer.parseInt(properties.getProperty("threads.pool.size", "8").trim());
    }
}
