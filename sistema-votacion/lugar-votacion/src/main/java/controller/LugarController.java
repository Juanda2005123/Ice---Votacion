package controller;

import model.Voto;
import config.ConfiguracionLugar;
import comunicacion.ServicioComunicacionLugar;
import comunicacion.ServicioVerificacionConectividad;

/**
 * Controlador principal del lugar de votacion que maneja el reenvio de votos.
 * 
 * Este controlador es responsable de:
 * - Recibir votos desde el broker mesa-lugar
 * - Verificar conectividad con el broker destino
 * - Reenviar votos al broker lugar-departamento
 * 
 * El controlador no realiza validaciones de negocio ni almacenamiento,
 * solo actua como intermediario en la cadena de comunicacion.
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-06-14
 */
public class LugarController {
    
    private ServicioComunicacionLugar comunicacion;
    private ServicioVerificacionConectividad verificador;
    private ConfiguracionLugar config;

    /**
     * Constructor que inicializa el controlador con la configuracion del lugar.
     * 
     * @param config Configuracion del lugar con destino y parametros
     */
    public LugarController(ConfiguracionLugar config) {
        this.config = config;
        this.comunicacion = new ServicioComunicacionLugar(config);
        this.verificador = new ServicioVerificacionConectividad();
    }
    
    /**
     * Verifica la conectividad con el broker destino al iniciar el lugar de votacion.
     * Muestra informacion detallada del broker destino (consistente con el broker).
     */
    public void verificarConectividadInicial() {
        System.out.println("=== VERIFICANDO CONECTIVIDAD CON BROKER DESTINO ===");
        
        boolean conectado = verificador.verificarConectividad(config);        if (conectado) {
            // Mostrar informacion detallada del broker destino conectado (consistente con broker)
            System.out.println("[OK] Conexion exitosa con broker lugar-departamento en " + 
                             config.getBrokerDestinoHost() + ":" + config.getBrokerDestinoPuerto());
        } else {
            // Mostrar advertencia para broker destino no conectado (consistente con broker)
            System.out.println("[!] ADVERTENCIA: No se pudo conectar con broker lugar-departamento en " + 
                             config.getBrokerDestinoHost() + ":" + config.getBrokerDestinoPuerto());
        }
        
        System.out.println("=== VERIFICACION DE CONECTIVIDAD COMPLETADA ===");
    }
    
    /**
     * Funcion principal que recibe un voto y lo reenvia al broker destino.
     * No realiza validaciones de negocio ni almacenamiento, solo reenvio.
     * 
     * @param voto Voto a procesar y reenviar
     * @return true si el voto fue reenviado exitosamente, false en caso contrario
     */
    public boolean procesarVoto(Voto voto) {
        // Solo reenviar el voto
        return comunicacion.reenviarVoto(voto);
    }

    public int validarVoto(String documento, Integer candidatoId) {
        // Solo reenviar el voto
        return comunicacion.reenviarValidacionVotante(documento, candidatoId);
    }
    
    /**
     * Verifica conectividad con el broker destino.
     * Util para diagnostico y monitoreo del estado de la conexion.
     */
    public void verificarEstadoDestinos() {
        verificador.verificarConectividadDestino(config);
    }
    
    /**
     * Verifica conectividad con el broker destino.
     * Metodo de compatibilidad para verificacion directa.
     * 
     * @return true si la conexion es exitosa, false en caso contrario
     */
    public boolean verificarConectividadBroker() {
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
        System.out.println("LugarController " + config.getLugarId() + " cerrado");
    }
}
