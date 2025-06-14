package controller;

import model.Voto;
import config.ConfiguracionBroker;
import comunicacion.ServicioComunicacionBroker;

/**
 * Controlador principal del broker que maneja UNICAMENTE:
 * - Recibir votos
 * - Enviar votos
 * NO valida, NO guarda, NO procesa - SOLO REENVIA
 */
public class BrokerController {
    
    private ServicioComunicacionBroker comunicacion;
    
    public BrokerController(ConfiguracionBroker config) {
        this.comunicacion = new ServicioComunicacionBroker(config);
        System.out.println("BrokerController inicializado - SOLO reenvio de votos");
    }
    
    /**
     * UNICA FUNCION: Recibe un voto y lo reenvia al destino
     * SIN validaciones, SIN guardar, SIN estadisticas
     */
    public boolean procesarVoto(Voto voto) {
        System.out.println("Reenviando voto ID: " + voto.getId() + 
                         " para candidato: " + voto.getCandidato().getNombre());
        
        // SOLO reenviar - nada mas
        return comunicacion.reenviarVoto(voto);
    }
    
    /**
     * Verifica conectividad con destinos (esto si es util para diagnostico)
     */
    public void verificarEstadoDestinos() {
        comunicacion.verificarConectividadDestinos();
    }
    
    /**
     * Cierra las conexiones
     */
    public void cerrar() {
        if (comunicacion != null) {
            comunicacion.cerrarConexion();
        }
        System.out.println("BrokerController cerrado");
    }
}
