package controller;

import model.Voto;
import config.ConfiguracionBroker;
import comunicacion.ServicioComunicacionBroker;
import comunicacion.ServicioVerificacionConectividad;
import enrutamiento.EstrategiaEnrutamiento;

/**
 * Controlador principal del broker que maneja el reenvio de votos.
 * 
 * Este controlador es responsable de:
 * - Recibir votos desde el servidor Ice
 * - Verificar conectividad con destinos configurados
 * - Reenviar votos a destinos activos segun la estrategia de enrutamiento
 * 
 * El controlador no realiza validaciones de negocio ni almacenamiento,
 * solo actua como intermediario para el reenvio de votos.
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-06-14
 */
public class BrokerController {    
    private ServicioComunicacionBroker comunicacion;
    private ServicioVerificacionConectividad verificador;
    private ConfiguracionBroker config;
    private EstrategiaEnrutamiento estrategia;

    /**
     * Constructor que inicializa el controlador con la configuracion del broker.
     * 
     * @param config Configuracion del broker con destinos y parametros
     */
    public BrokerController(ConfiguracionBroker config) {
        this.config = config;
        this.comunicacion = new ServicioComunicacionBroker(config);
        this.verificador = new ServicioVerificacionConectividad();
        this.estrategia = new EstrategiaEnrutamiento(config);
    }    /**
     * Verifica la conectividad con todos los destinos al iniciar el broker.
     * Muestra informacion detallada de cada destino verificado.
     */
    public void verificarConectividadInicial() {
        System.out.println("=== VERIFICANDO CONECTIVIDAD CON DESTINOS ===");
        
        for (ConfiguracionBroker.Destino destino : config.getDestinosActivos()) {
            boolean conectado = verificador.verificarConectividad(destino);
            if (conectado) {
                // Mostrar informacion detallada del destino conectado
                System.out.println("[OK] Conexion exitosa con " + destino.getId() + 
                                 " (" + destino.getTipo() + ") en " + 
                                 destino.getHost() + ":" + destino.getPuerto());
            } else {
                // Mostrar advertencia para destinos no conectados
                System.out.println("[!] ADVERTENCIA: No se pudo conectar con " + destino.getId() + 
                                 " (" + destino.getTipo() + ") en " + 
                                 destino.getHost() + ":" + destino.getPuerto());
            }
        }
        
        System.out.println("=== VERIFICACION DE CONECTIVIDAD COMPLETADA ===");
    }
    
    /**
     * Funcion principal que recibe un voto y lo reenvia al destino correspondiente.
     * No realiza validaciones de negocio ni almacenamiento, solo reenvio.
     * A diferencia del lugar de votacion, puede verificar conectividad antes de enviar.
     * 
     * @param voto Voto a procesar y reenviar
     * @return true si el voto fue reenviado exitosamente, false en caso contrario
     */
    public boolean procesarVoto(Voto voto) {
        System.out.println(voto.getCandidato().getNombre());
        // Solo reenviar el voto
        return comunicacion.reenviarVoto(voto);
    }    /**
     * Valida un voto reenviando la solicitud al destino correspondiente.
     * Por ahora retorna 1 temporalmente, pero el flujo esta preparado para
     * recibir códigos 0-3 desde el servidor central.
     * 
     * @param documento Documento del votante como String
     * @param candidatoId ID del candidato elegido
     * @return Código de validación: 0=puede votar, 1=no es su mesa, 2=ya votó, 3=no existe
     */
    public int validarVoto(String documento, Integer candidatoId) {
        // Por ahora siempre retorna 1 (no es su mesa) temporalmente
        // TODO: Cuando se conecte al servidor central, esto cambiará
        
        // Verificar si estamos en el broker final (departamento)
        if (config.getBrokerNombre().toLowerCase().contains("departamento")) {
            // Este es el broker final - por ahora retorna 1 temporalmente
            return 1000;
        }
        
        // Si no es el broker final, reenviar la validación
        return comunicacion.reenviarValidacionVotante(documento, candidatoId);
    }
    
    /**
     * Verifica si un destino especifico esta disponible antes de enviar.
     * Esta capacidad solo existe en el broker, no en el lugar de votacion.
     * 
     * @param destino Destino a verificar
     * @return true si el destino esta disponible, false en caso contrario
     */
    public boolean verificarDestinoAntesDEnvio(ConfiguracionBroker.Destino destino) {
        return verificador.verificarConectividad(destino);
    }
    
    /**
     * Verifica conectividad con todos los destinos configurados.
     * Util para diagnostico y monitoreo del estado de la red.
     */
    public void verificarEstadoDestinos() {
        verificador.verificarConectividadDestinos(config);
    }    /**
     * Cierra todas las conexiones y libera recursos.
     */
    public void cerrar() {
        if (verificador != null) {
            verificador.cerrar();
        }
        if (estrategia != null) {
            estrategia.cerrar();
        }
        if (comunicacion != null) {
            comunicacion.cerrarConexion();
        }
    }
}
