package controller;

import model.Voto;
import config.ConfiguracionLugar;
import comunicacion.ServicioComunicacionLugar;
import comunicacion.ServicioVerificacionConectividad;

/**
 * Controlador principal del lugar de votación que maneja UNICAMENTE:
 * - Recibir votos (del broker mesa-lugar)
 * - Reenviar votos (al broker lugar-departamento)
 * - Verificar conectividad con broker destino al inicio
 * NO valida, NO guarda, NO procesa - SOLO REENVIA
 * FUNCIÓN: Actúa como intermediario en la cadena de comunicación
 */
public class LugarController {
    
    private ServicioComunicacionLugar comunicacion;
    private ServicioVerificacionConectividad verificador;
    private ConfiguracionLugar config;

    public LugarController(ConfiguracionLugar config) {
        this.config = config;
        this.comunicacion = new ServicioComunicacionLugar(config);
        this.verificador = new ServicioVerificacionConectividad();
    }
    
    /**
     * VERIFICA la conectividad con el broker destino al iniciar el lugar de votación.
     * Muestra información detallada del broker destino (consistente con el broker).
     */
    public void verificarConectividadInicial() {
        System.out.println("=== VERIFICANDO CONECTIVIDAD CON BROKER DESTINO ===");
        
        boolean conectado = verificador.verificarConectividad(config);
        
        if (conectado) {
            // Mostrar información detallada del broker destino conectado (consistente con broker)
            System.out.println("✓ Conexión exitosa con broker lugar-departamento en " + 
                             config.getBrokerDestinoHost() + ":" + config.getBrokerDestinoPuerto());
        } else {
            // Mostrar advertencia para broker destino no conectado (consistente con broker)
            System.out.println("✗ ADVERTENCIA: No se pudo conectar con broker lugar-departamento en " + 
                             config.getBrokerDestinoHost() + ":" + config.getBrokerDestinoPuerto());
        }
        
        System.out.println("=== VERIFICACIÓN DE CONECTIVIDAD COMPLETADA ===");
    }    /**
     * UNICA FUNCION: Recibe un voto y lo reenvia al broker destino
     * SIN validaciones, SIN guardar, SIN estadisticas
     */
    public boolean procesarVoto(Voto voto) {
        // SOLO reenviar - nada mas
        return comunicacion.reenviarVoto(voto);
    }
    
    /**
     * Verifica conectividad con broker destino (esto si es util para diagnostico)
     */
    public void verificarEstadoDestinos() {
        verificador.verificarConectividadDestino(config);
    }
    
    /**
     * Verifica conectividad con el broker destino (método de compatibilidad)
     */
    public boolean verificarConectividadBroker() {
        return verificador.verificarConectividad(config);
    }
    
    /**
     * Cierra las conexiones
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
