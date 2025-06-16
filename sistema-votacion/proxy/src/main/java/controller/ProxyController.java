package controller;

import config.ConfiguracionProxy;
import database.ValidadorCiudadanos;

/**
 * Controlador principal del proxy de validacion que maneja validaciones de ciudadanos.
 * 
 * Este controlador es responsable de:
 * - Recibir validaciones de ciudadanos desde nodos anteriores
 * - Validar ciudadanos contra la base de datos PostgreSQL
 * - Retornar códigos de validación estándar
 * 
 * El controlador NO maneja votos, solo validaciones de ciudadanos,
 * consultando directamente la base de datos configurada.
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-06-15
 */
public class ProxyController {
    
    private ValidadorCiudadanos validador;
    private ConfiguracionProxy config;

    /**
     * Constructor que inicializa el controlador con la configuracion del proxy.
     * 
     * @param config Configuracion del proxy con parametros de base de datos
     */
    public ProxyController(ConfiguracionProxy config) {        
        this.config = config;
        this.validador = new ValidadorCiudadanos(config);
    }    /**
     * Verifica la conectividad con la base de datos al iniciar el proxy.
     * Muestra informacion detallada de la conexion a PostgreSQL.
     */
    public void verificarConectividadInicial() {
        System.out.println("=== VERIFICANDO CONECTIVIDAD CON BASE DE DATOS ===");
        
        boolean conectado = validador.verificarConexion();
        if (conectado) {
            // Mostrar informacion detallada de la base de datos conectada
            System.out.println("[OK] Conexion exitosa con PostgreSQL");
            System.out.println("- Host: " + config.getDbHost() + ":" + config.getDbPuerto());
            System.out.println("- Base de datos: " + config.getDbNombre());
            System.out.println("- Usuario: " + config.getDbUsuario());
            System.out.println("- Tabla: " + config.getDbEsquema() + "." + config.getDbTablaCiudadanos());
        } else {
            // Mostrar advertencia para base de datos no conectada
            System.out.println("[!] ADVERTENCIA: No se pudo conectar con PostgreSQL");
            System.out.println("- Host: " + config.getDbHost() + ":" + config.getDbPuerto());
            System.out.println("- Base de datos: " + config.getDbNombre());
        }
        
        System.out.println("=== VERIFICACION DE CONECTIVIDAD COMPLETADA ===");
    }
    
    /**
     * Procesa un voto - NO IMPLEMENTADO EN PROXY.
     * El proxy solo maneja validaciones de ciudadanos, no votos.
     * 
     * @param voto Voto a procesar
     * @return No retorna, lanza excepcion
     * @throws UnsupportedOperationException Siempre, ya que no se utiliza en proxy
     */
    public boolean procesarVoto(model.Voto voto) {
        throw new UnsupportedOperationException("El procesamiento de votos no se implementa en el proxy. Solo se procesan validaciones de ciudadanos.");
    }    /**
     * Valida un ciudadano consultando la base de datos PostgreSQL.
     * El proxy consulta directamente la base de datos sin reenvío.
     * 
     * @param documento Documento del votante como String
     * @param candidatoId ID del candidato elegido (no utilizado en validación)
     * @return Codigo de validacion de la base de datos:
     *         1 = Ciudadano válido (encontrado en BD)
     *         3 = Ciudadano no encontrado en BD
     *         4 = Error de procesamiento
     */
    public int validarCiudadano(String documento, Integer candidatoId) {
        // Validar ciudadano contra la base de datos
        return validador.validarCiudadano(documento);
    }
    
     /**
     * Verifica el estado de la conexión con la base de datos.
     * Útil para diagnóstico y monitoreo del estado de la conexión.
     */
    public void verificarEstadoBaseDatos() {
        boolean conectado = validador.verificarConexion();
        if (conectado) {
            System.out.println("[PROXY] Base de datos PostgreSQL: CONECTADA");
            System.out.println("[PROXY] " + validador.getEstadisticas());
        } else {
            System.out.println("[PROXY] Base de datos PostgreSQL: DESCONECTADA");
        }
    }
    
    /**
     * Verifica conectividad con la base de datos.
     * Método de compatibilidad para verificación directa.
     * 
     * @return true si la conexión es exitosa, false en caso contrario
     */
    public boolean verificarConectividadBaseDatos() {
        return validador.verificarConexion();
    }
    
    /**
     * Cierra todas las conexiones y libera recursos.
     * Debe llamarse al finalizar el uso del controlador.
     */
    public void cerrar() {
        if (validador != null) {
            validador.cerrar();
        }
        System.out.println("ProxyController " + config.getProxyId() + " cerrado");
    }
}
