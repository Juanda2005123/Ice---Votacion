package controller;

import VotingSystem.DeltaConteo;
import config.ConfiguracionBroker;
import comunicacion.ServicioComunicacionBroker;
import comunicacion.ServicioVerificacionConectividad;
import enrutamiento.EstrategiaEnrutamiento;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Controlador principal del broker que maneja el reenvio de deltas con Thread Pool.
 * 
 * Este controlador es responsable de:
 * - Recibir deltas desde el servidor Ice
 * - Verificar conectividad con destinos configurados
 * - Reenviar deltas a destinos activos usando Thread Pool para paralelismo
 * - Mantener load balancing LRU implementado
 * 
 * @author Sistema de Votacion
 * @version 2.0 - Map-Reduce Delta System con Thread Pool
 * @since 2025-06-15
 */
public class BrokerController {    
    private ServicioComunicacionBroker comunicacion;
    private ServicioVerificacionConectividad verificador;
    private ConfiguracionBroker config;
    private EstrategiaEnrutamiento estrategia;
    private ExecutorService threadPool;

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
        
        // Inicializar Thread Pool para reenvio paralelo de deltas
        int poolSize = config.getThreadPoolSize();
        this.threadPool = Executors.newFixedThreadPool(poolSize);
    }    /**
     * Verifica la conectividad con todos los destinos al iniciar el broker.
     */
    public void verificarConectividadInicial() {
        for (ConfiguracionBroker.Destino destino : config.getDestinosActivos()) {
            boolean conectado = verificador.verificarConectividad(destino);
            if (conectado) {
                // Solo log de conexión exitosa sin detalles
            } else {
                // Solo log de advertencia sin detalles
            }
        }
    }
    
    /**
     * Procesa un delta recibido usando Thread Pool para reenvio paralelo.
     * No realiza validaciones, solo reenvio con load balancing LRU.
     * 
     * @param delta Delta a procesar y reenviar
     * @return true si el delta fue enviado para procesamiento, false en caso contrario
     */
    public boolean procesarDelta(DeltaConteo delta) {
        try {
            // Enviar delta para procesamiento asíncrono en Thread Pool
            threadPool.submit(() -> {
                comunicacion.reenviarDelta(delta);
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }    /**
     * Valida un ciudadano enviando la solicitud al proxy de validacion.
     * Se conecta al proxy configurado para verificar si el ciudadano existe.
     * 
     * @param documento Documento del ciudadano a validar
     * @param candidatoId ID del candidato elegido (no utilizado en validacion)
     * @return Código de validación del proxy: 1=existe, 3=no existe, 4=error
     */
    public int validarCiudadano(String documento, Integer candidatoId) {
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
    }

    /**
     * Cierra todas las conexiones y libera recursos incluyendo Thread Pool.
     */
    public void cerrar() {
        // Cerrar Thread Pool ordenadamente
        if (threadPool != null) {
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
            }
        }
        
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