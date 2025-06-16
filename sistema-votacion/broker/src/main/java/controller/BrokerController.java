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
        // Solo reenviar el voto
        return comunicacion.reenviarVoto(voto);
    }    /**
     * Valida un ciudadano enviando la solicitud al proxy de validacion.
     * Se conecta al proxy configurado para verificar si el ciudadano existe.
     * 
     * @param documento Documento del ciudadano a validar
     * @param candidatoId ID del candidato elegido (no utilizado en validacion)
     * @return Código de validación del proxy: 1=existe, 3=no existe, 4=error
     */
    public int validarCiudadano(String documento, Integer candidatoId) {
        // Enviar validacion al proxy configurado
        return comunicacion.enviarValidacionAProxy(documento, candidatoId);
    }
    
    /**
     * Método de compatibilidad con la interfaz existente.
     * Redirige a validarCiudadano.
     * 
     * @param documento Documento del votante como String
     * @param candidatoId ID del candidato elegido
     * @return Codigo de validacion (1, 3, 4)
     */
    public int validarVoto(String documento, Integer candidatoId) {
        return validarCiudadano(documento, candidatoId);
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