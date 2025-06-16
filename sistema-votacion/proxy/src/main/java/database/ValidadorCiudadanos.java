package database;

import config.ConfiguracionProxy;
import java.sql.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Servicio para validar ciudadanos contra la base de datos PostgreSQL.
 * 
 * Esta clase es responsable de:
 * - Establecer conexion con la base de datos PostgreSQL
 * - Validar si un documento de ciudadano existe en la base de datos
 * - Gestionar el pool de conexiones y timeouts
 * - Manejar errores de conexion y queries
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-06-15
 */
public class ValidadorCiudadanos {
    
    private final ConfiguracionProxy config;
    private final AtomicInteger conexionesActivas = new AtomicInteger(0);
      // Códigos de respuesta estándar
    public static final int CIUDADANO_VALIDO = 1;        // Ciudadano encontrado y válido
    public static final int CIUDADANO_NO_ENCONTRADO = 3; // Ciudadano no existe en BD
    public static final int ERROR_PROCESAMIENTO = 4;     // Error general de procesamiento
    
    /**
     * Constructor que inicializa el validador con la configuración del proxy.
     * 
     * @param config Configuración del proxy con parámetros de base de datos
     */
    public ValidadorCiudadanos(ConfiguracionProxy config) {
        this.config = config;
        inicializarDriver();
        verificarConexionInicial();
    }
    
    /**
     * Inicializa el driver JDBC de PostgreSQL.
     */
    private void inicializarDriver() {
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("[DB] Driver PostgreSQL inicializado correctamente");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Error: Driver PostgreSQL no encontrado. " +
                    "Asegúrese de que postgresql.jar esté en el classpath", e);
        }
    }
    
    /**
     * Verifica la conexión inicial con la base de datos.
     */
    private void verificarConexionInicial() {
        try (Connection conn = obtenerConexion()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("[DB] Conexión inicial exitosa con PostgreSQL");
                System.out.println("[DB] Base de datos: " + config.getDbNombre() + 
                                 " en " + config.getDbHost() + ":" + config.getDbPuerto());
            }
        } catch (SQLException e) {
            System.err.println("[DB] ADVERTENCIA: No se pudo establecer conexión inicial: " + e.getMessage());
        }
    }    /**
     * Valida si un ciudadano existe en la base de datos.
     * 
     * @param documento Documento del ciudadano a validar
     * @return Código de validación: 1=existe, 3=no existe, 4=error
     */
    public int validarCiudadano(String documento) {
        System.out.println("[DEBUG] Iniciando validacion de documento: '" + documento + "'");
        
        // Validaciones básicas de entrada
        if (documento == null) {
            System.out.println("[DEBUG] ERROR: Documento es NULL");
            return ERROR_PROCESAMIENTO;
        }
        
        if (documento.trim().isEmpty()) {
            System.out.println("[DEBUG] ERROR: Documento esta vacio despues de trim");
            return ERROR_PROCESAMIENTO;
        }
        
        // NO hacer trim para mantener formato original
        System.out.println("[DEBUG] Documento a buscar: '" + documento + "' (longitud: " + documento.length() + ")");
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            System.out.println("[DEBUG] Intentando obtener conexion...");
            // Obtener conexión
            conn = obtenerConexion();
            if (conn == null) {
                System.out.println("[DEBUG] ERROR: No se pudo obtener conexion");
                return ERROR_PROCESAMIENTO;
            }
            System.out.println("[DEBUG] Conexion obtenida exitosamente");
              // Construir query parametrizada usando EXISTS (más eficiente que COUNT)
            String sql = String.format("SELECT EXISTS(SELECT 1 FROM %s.%s WHERE %s = ?)",
                    config.getDbEsquema(),
                    config.getDbTablaCiudadanos(),
                    config.getDbColumnaDocumento());
            
            System.out.println("[DEBUG] SQL query: " + sql);
            System.out.println("[DEBUG] Parametro: '" + documento + "'");
            
            stmt = conn.prepareStatement(sql);
            stmt.setQueryTimeout(config.getDbTimeoutQuery() / 1000); // Convertir ms a segundos
            stmt.setString(1, documento);
            
            System.out.println("[DEBUG] Ejecutando query...");
            // Ejecutar query
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                boolean exists = rs.getBoolean(1);
                System.out.println("[DEBUG] Query ejecutada. EXISTS resultado: " + exists);
                
                if (exists) {
                    System.out.println("[DEBUG] RESULTADO: Ciudadano EXISTE (codigo 1)");
                    return CIUDADANO_VALIDO; // 1 = existe
                } else {
                    System.out.println("[DEBUG] RESULTADO: Ciudadano NO EXISTE (codigo 3)");
                    return CIUDADANO_NO_ENCONTRADO; // 3 = no existe
                }
            } else {
                System.out.println("[DEBUG] ERROR: Query no retorno resultados");
                return ERROR_PROCESAMIENTO; // 4 = error
            }
            
        } catch (SQLException e) {
            System.out.println("[DEBUG] ERROR SQL: " + e.getMessage());
            System.out.println("[DEBUG] SQL State: " + e.getSQLState());
            System.out.println("[DEBUG] Error Code: " + e.getErrorCode());
            return ERROR_PROCESAMIENTO; // 4 = error
        } catch (Exception e) {
            System.out.println("[DEBUG] ERROR GENERAL: " + e.getMessage());
            e.printStackTrace();
            return ERROR_PROCESAMIENTO; // 4 = error
        } finally {
            System.out.println("[DEBUG] Cerrando recursos...");
            // Cerrar recursos en orden inverso
            cerrarRecursos(rs, stmt, conn);
        }
    }
    
    /**
     * Obtiene una conexión a la base de datos con timeout.
     * 
     * @return Conexión a la base de datos o null si falla
     */
    private Connection obtenerConexion() throws SQLException {        try {
            System.out.println("[DEBUG] Verificando pool de conexiones...");
            // Controlar número de conexiones activas
            if (conexionesActivas.get() >= config.getDbPoolMaximo()) {
                System.err.println("[DB] Pool de conexiones lleno (" + config.getDbPoolMaximo() + ")");
                return null;
            }
            
            System.out.println("[DEBUG] Construyendo URL de conexion...");
            String url = config.getDbUrl();
            System.out.println("[DEBUG] URL: " + url);
            System.out.println("[DEBUG] Usuario: " + config.getDbUsuario());
            
            // Configurar propiedades de conexión
            java.util.Properties props = new java.util.Properties();
            props.setProperty("user", config.getDbUsuario());
            props.setProperty("password", config.getDbPassword());
            props.setProperty("loginTimeout", String.valueOf(config.getDbTimeoutConexion() / 1000));
            props.setProperty("socketTimeout", String.valueOf(config.getDbTimeoutQuery() / 1000));
            props.setProperty("tcpKeepAlive", "true");
            
            System.out.println("[DEBUG] Intentando conectar a PostgreSQL...");
            Connection conn = DriverManager.getConnection(url, props);
            conexionesActivas.incrementAndGet();
            System.out.println("[DEBUG] Conexion establecida. Conexiones activas: " + conexionesActivas.get());
            
            return conn;
            
        } catch (SQLException e) {
            System.err.println("[DEBUG] ERROR SQL obteniendo conexión:");
            System.err.println("[DEBUG] Mensaje: " + e.getMessage());
            System.err.println("[DEBUG] SQL State: " + e.getSQLState());
            System.err.println("[DEBUG] Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return null; // Retornar null en lugar de lanzar excepción
        }
    }
      /**
     * Cierra todos los recursos JDBC de forma segura.
     * 
     * @param rs ResultSet a cerrar
     * @param stmt Statement a cerrar
     * @param conn Connection a cerrar
     */
    private void cerrarRecursos(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                // Silencioso - no imprimir error
            }
        }
        
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                // Silencioso - no imprimir error
            }
        }
        
        if (conn != null) {
            try {
                conn.close();
                conexionesActivas.decrementAndGet();
            } catch (SQLException e) {
                // Silencioso - no imprimir error
            }
        }
    }
      /**
     * Verifica si la conexión a la base de datos está disponible.
     * 
     * @return true si la conexión es exitosa, false en caso contrario
     */
    public boolean verificarConexion() {
        try (Connection conn = obtenerConexion()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Obtiene estadísticas del validador.
     * 
     * @return String con estadísticas de conexiones activas
     */
    public String getEstadisticas() {
        return String.format("Conexiones activas: %d/%d", 
                           conexionesActivas.get(), config.getDbPoolMaximo());
    }
    
    /**
     * Libera recursos y cierra conexiones pendientes.
     */
    public void cerrar() {
        System.out.println("[DB] ValidadorCiudadanos cerrado. " + getEstadisticas());
    }
}
