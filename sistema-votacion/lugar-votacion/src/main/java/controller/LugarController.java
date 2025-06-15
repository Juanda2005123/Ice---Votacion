package controller;

import model.Voto;
import config.ConfiguracionLugar;
import comunicacion.ServicioComunicacionLugar;

/**
 * Controlador principal del lugar de votación que maneja:
 * - Recibir votos (del broker mesa-lugar)
 * - Reenviar votos (al broker lugar-departamento)
 * FUNCIÓN: Actúa como intermediario en la cadena de comunicación
 */
public class LugarController {
    
    private ServicioComunicacionLugar comunicacion;
    private ConfiguracionLugar config;

    public LugarController(ConfiguracionLugar config) {
        this.config = config;
        this.comunicacion = new ServicioComunicacionLugar(config);
    }
    
    /**
     * FUNCIÓN PRINCIPAL: Recibe un voto y lo reenvía al siguiente broker
     * Sin validaciones - SOLO reenvío directo
     */
    public boolean procesarVoto(Voto voto) {
        // Debug: mostrar que el lugar recibió el voto
        System.out.println("LUGAR " + config.getLugarId() + " recibió voto ID: " + voto.getId() + 
                         " para candidato: " + voto.getCandidato().getNombre());
        
        // Reenviar inmediatamente al broker lugar-departamento
        return comunicacion.reenviarVoto(voto);
    }
    
    /**
     * Verifica conectividad con el broker destino
     */
    public boolean verificarConectividadBroker() {
        return comunicacion.verificarConectividad();
    }
    
    /**
     * Cierra las conexiones
     */
    public void cerrar() {
        if (comunicacion != null) {
            comunicacion.cerrarConexion();
        }
        System.out.println("LugarController " + config.getLugarId() + " cerrado");
    }
}
