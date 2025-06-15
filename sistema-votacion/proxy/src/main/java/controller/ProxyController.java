package controller;

import config.ConfiguracionProxy;
import comunicacion.ServicioComunicacionProxy;
import comunicacion.ServicioVerificacionConectividad;

/**
 * Controlador principal del proxy de validacion que maneja el reenvio de validaciones de ciudadanos.
 * 
 * Este controlador es responsable de:
 * - Recibir validaciones de ciudadanos desde nodos anteriores
 * - Verificar conectividad con el nodo destino configurado
 * - Reenviar validaciones al nodo destino
 * 
 * El controlador NO maneja votos, solo validaciones de ciudadanos,
 * actuando como intermediario en la cadena de validacion.
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-06-15
 */
public class ProxyController {
    
    private ServicioComunicacionProxy comunicacion;
    private ServicioVerificacionConectividad verificador;
    private ConfiguracionProxy config;

    /**
     * Constructor que inicializa el controlador con la configuracion del proxy.
     * 
     * @param config Configuracion del proxy con destino y parametros
     */
    public ProxyController(ConfiguracionProxy config) {        
        this.config = config;
        this.comunicacion = new ServicioComunicacionProxy(config);
        this.verificador = new ServicioVerificacionConectividad();
    }    
    /**
     * Verifica la conectividad con el nodo destino al iniciar el proxy.
     * Muestra informacion detallada del nodo destino.
     */
    public void verificarConectividadInicial() {
        System.out.println("=== VERIFICANDO CONECTIVIDAD CON NODO DESTINO ===");
        
        boolean conectado = verificador.verificarConectividad(config);
        if (conectado) {
            // Mostrar informacion detallada del nodo destino conectado
            System.out.println("[OK] Conexion exitosa con nodo destino en " + 
                             config.getNodoDestinoHost() + ":" + config.getNodoDestinoPuerto());
        } else {
            // Mostrar advertencia para nodo destino no conectado
            System.out.println("[!] ADVERTENCIA: No se pudo conectar con nodo destino en " + 
                             config.getNodoDestinoHost() + ":" + config.getNodoDestinoPuerto());
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
    }
    
    /**
     * Valida un ciudadano reenviando la solicitud al nodo destino.
     * El proxy actua como intermediario sin validacion local.
     * 
     * @param documento Documento del votante como String
     * @param candidatoId ID del candidato elegido
     * @return Codigo de validacion del nodo destino (0-3)
     */
    public int validarVoto(String documento, Integer candidatoId) {
        // Solo reenviar la validacion al nodo destino
        return comunicacion.reenviarValidacionVotante(documento, candidatoId);
    }    
    /**
     * Verifica conectividad con el nodo destino.
     * Util para diagnostico y monitoreo del estado de la conexion.
     */
    public void verificarEstadoDestinos() {
        verificador.verificarConectividadDestino(config);
    }
    
    /**
     * Verifica conectividad con el nodo destino.
     * Metodo de compatibilidad para verificacion directa.
     * 
     * @return true si la conexion es exitosa, false en caso contrario
     */
    public boolean verificarConectividadDestino() {
        return verificador.verificarConectividad(config);
    }
    
    /**
     * Cierra todas las conexiones y libera recursos.
     * Debe llamarse al finalizar el uso del controlador.
     */
    public void cerrar() {
        if (verificador != null) {
            verificador.cerrar();
        }
        if (comunicacion != null) {
            comunicacion.cerrarConexion();
        }
        System.out.println("ProxyController " + config.getProxyId() + " cerrado");
    }
}
