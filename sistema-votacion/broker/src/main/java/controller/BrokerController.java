package controller;

import model.Voto;
import config.ConfiguracionBroker;
import comunicacion.ServicioComunicacionBroker;
import comunicacion.ServicioVerificacionConectividad;

/**
 * Controlador principal del broker que maneja UNICAMENTE:
 * - Recibir votos
 * - Enviar votos
 * - Verificar conectividad con destinos al inicio
 * NO valida, NO guarda, NO procesa - SOLO REENVIA
 */
public class BrokerController {
    
    private ServicioComunicacionBroker comunicacion;
    private ServicioVerificacionConectividad verificador;
    private ConfiguracionBroker config;

    public BrokerController(ConfiguracionBroker config) {
        this.config = config;
        this.comunicacion = new ServicioComunicacionBroker(config);
        this.verificador = new ServicioVerificacionConectividad();
    }
    
    /**
     * VERIFICA la conectividad con todos los destinos al iniciar el broker.
     * Muestra información detallada de cada destino.
     */
    public void verificarConectividadInicial() {
        System.out.println("=== VERIFICANDO CONECTIVIDAD CON DESTINOS ===");
        
        for (ConfiguracionBroker.Destino destino : config.getDestinosActivos()) {
            boolean conectado = verificador.verificarConectividad(destino);
            
            if (conectado) {
                // Mostrar información detallada del destino conectado
                System.out.println("✓ Conexión exitosa con " + destino.getId() + 
                                 " (" + destino.getTipo() + ") en " + 
                                 destino.getHost() + ":" + destino.getPuerto());
            } else {
                // Mostrar advertencia para destinos no conectados
                System.out.println("✗ ADVERTENCIA: No se pudo conectar con " + destino.getId() + 
                                 " (" + destino.getTipo() + ") en " + 
                                 destino.getHost() + ":" + destino.getPuerto());
            }
        }
        
        System.out.println("=== VERIFICACIÓN DE CONECTIVIDAD COMPLETADA ===");
    }
      /**
     * UNICA FUNCION: Recibe un voto y lo reenvia al destino
     * SIN validaciones, SIN guardar, SIN estadisticas
     * DIFERENCIA CON LUGAR: Puede verificar conectividad antes de enviar
     */
    public boolean procesarVoto(Voto voto) {
        // SOLO reenviar - nada mas
        return comunicacion.reenviarVoto(voto);
    }
    
    /**
     * Verifica si un destino específico está disponible antes de enviar
     * DIFERENCIA CON LUGAR: Esta capacidad solo existe en el broker
     */
    public boolean verificarDestinoAntesDEnvio(ConfiguracionBroker.Destino destino) {
        return verificador.verificarConectividad(destino);
    }
      /**
     * Verifica conectividad con destinos (esto si es util para diagnostico)
     */
    public void verificarEstadoDestinos() {
        verificador.verificarConectividadDestinos(config);
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
        System.out.println("BrokerController cerrado");
    }
}
