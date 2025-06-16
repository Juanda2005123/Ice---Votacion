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
    }      // Configuracion de la Base de Datos PostgreSQL
    
    /**
     * Obtiene el host de la base de datos PostgreSQL.
     * 
     * @return Host de la base de datos
     */
    public String getDbHost() {
        return properties.getProperty("db.host", "localhost");
    }
    
    /**
     * Obtiene el puerto de la base de datos PostgreSQL.
     * 
     * @return Puerto de la base de datos
     */
    public int getDbPuerto() {
        return Integer.parseInt(properties.getProperty("db.puerto", "5432"));
    }
    
    /**
     * Obtiene el nombre de la base de datos.
     * 
     * @return Nombre de la base de datos
     */
    public String getDbNombre() {
        return properties.getProperty("db.nombre", "votacion_db");
    }
    
    /**
     * Obtiene el usuario de la base de datos.
     * 
     * @return Usuario de la base de datos
     */
    public String getDbUsuario() {
        return properties.getProperty("db.usuario", "postgres");
    }
    
    /**
     * Obtiene la contraseña de la base de datos.
     * 
     * @return Contraseña de la base de datos
     */
    public String getDbPassword() {
        return properties.getProperty("db.password", "admin123");
    }
    
    /**
     * Obtiene el esquema de la base de datos.
     * 
     * @return Esquema de la base de datos
     */
    public String getDbEsquema() {
        return properties.getProperty("db.esquema", "public");
    }
      /**
     * Obtiene el nombre de la tabla de ciudadanos.
     * 
     * @return Nombre de la tabla de ciudadanos
     */
    public String getDbTablaCiudadanos() {
        return properties.getProperty("db.tabla.ciudadanos", "personas");
    }
    
    /**
     * Obtiene el nombre de la columna documento.
     * 
     * @return Nombre de la columna documento
     */
    public String getDbColumnaDocumento() {
        return properties.getProperty("db.columna.documento", "documento");
    }
    
    /**
     * Obtiene el tamaño minimo del pool de conexiones.
     * 
     * @return Tamaño minimo del pool
     */
    public int getDbPoolMinimo() {
        return Integer.parseInt(properties.getProperty("db.pool.minimo", "2"));
    }
    
    /**
     * Obtiene el tamaño maximo del pool de conexiones.
     * 
     * @return Tamaño maximo del pool
     */
    public int getDbPoolMaximo() {
        return Integer.parseInt(properties.getProperty("db.pool.maximo", "10"));
    }
    
    /**
     * Obtiene el timeout para conexiones a la base de datos.
     * 
     * @return Timeout de conexion en milisegundos
     */
    public int getDbTimeoutConexion() {
        return Integer.parseInt(properties.getProperty("db.timeout.conexion", "30000"));
    }
    
    /**
     * Obtiene el timeout para queries a la base de datos.
     * 
     * @return Timeout de query en milisegundos
     */
    public int getDbTimeoutQuery() {
        return Integer.parseInt(properties.getProperty("db.timeout.query", "15000"));
    }
    
    /**
     * Construye la URL de conexion JDBC para PostgreSQL.
     * 
     * @return URL de conexion JDBC completa
     */
    public String getDbUrl() {
        return String.format("jdbc:postgresql://%s:%d/%s", 
                           getDbHost(), getDbPuerto(), getDbNombre());
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
