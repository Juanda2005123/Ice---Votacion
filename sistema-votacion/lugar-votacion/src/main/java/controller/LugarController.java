package controller;

import VotingSystem.DeltaConteo;
import config.ConfiguracionLugar;
import comunicacion.ServicioComunicacionLugar;
import comunicacion.ServicioVerificacionConectividad;
import deltas.RepositorioDeltas;
import deltas.GeneradorDeltas;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Controlador principal del lugar de votación con Map-Reduce Level 1.
 * 
 * Este controlador implementa el primer nivel de consolidación Map-Reduce:
 * - MAP PHASE: Thread Pool recibe deltas de mesas, los consolida en paralelo
 * - REDUCE PHASE: Crea y envía deltas consolidados por umbral
 * - ESTADO COMPARTIDO: ConcurrentHashMap thread-safe para conteos
 * 
 * @author Sistema de Votacion
 * @version 2.0 - Map-Reduce Level 1
 * @since 2025-06-15
 */
public class LugarController {
    
    private ServicioComunicacionLugar comunicacion;
    private ServicioVerificacionConectividad verificador;
    private ConfiguracionLugar config;
    private ExecutorService threadPool;
    private RepositorioDeltas repositorioDeltas;
    private GeneradorDeltas generadorDeltas;

    /**
     * Constructor que inicializa el controlador con Map-Reduce.
     * 
     * @param config Configuracion del lugar con parametros Map-Reduce
     */
    public LugarController(ConfiguracionLugar config) {
        this.config = config;
        this.comunicacion = new ServicioComunicacionLugar(config);
        this.verificador = new ServicioVerificacionConectividad();
        
        // Inicializar Thread Pool para MAP PHASE
        int poolSize = config.getThreadPoolSize();
        this.threadPool = Executors.newFixedThreadPool(poolSize);
        
        // Inicializar sistema Map-Reduce
        this.repositorioDeltas = new RepositorioDeltas(config.getLugarId());
        this.generadorDeltas = new GeneradorDeltas(
            repositorioDeltas, 
            config, 
            this::enviarDeltaConsolidado
        );
    }    
    /**
     * Verifica la conectividad con el broker destino al iniciar el lugar de votacion.
     */
    public void verificarConectividadInicial() {
        boolean conectado = verificador.verificarConectividad(config);
        if (!conectado) {
            // Solo log de error crítico
        }
    }
    
    /**
     * MAP PHASE: Procesa un delta de mesa usando Thread Pool.
     * Cada hilo ejecuta la consolidación en paralelo.
     * 
     * @param delta Delta recibido de mesa de votación
     * @return true si el delta fue enviado para procesamiento
     */
    public boolean procesarDelta(DeltaConteo delta) {
        try {
            // Enviar para procesamiento asíncrono en Thread Pool (MAP PHASE)
            threadPool.submit(() -> {
                generadorDeltas.procesarDelta(delta);
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * CALLBACK: Envía delta consolidado al broker destino.
     * Llamado por GeneradorDeltas cuando alcanza umbral.
     * 
     * @param deltaConsolidado Delta consolidado listo para enviar
     */
    private void enviarDeltaConsolidado(DeltaConteo deltaConsolidado) {
        comunicacion.reenviarDelta(deltaConsolidado);
    }    /**
     * Valida un voto reenviando la solicitud al broker destino.
     * El lugar actua como intermediario sin validacion local.
     * 
     * @param documento Documento del votante como String
     * @param candidatoId ID del candidato elegido
     * @return Código de validación del broker destino (0-3)
     */
    public int validarVoto(String documento, Integer candidatoId) {
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
     * Cierra todas las conexiones y libera recursos incluyendo Thread Pool.
     * Envía último delta consolidado antes de cerrar.
     */
    public void cerrar() {
        // Enviar último delta antes de cerrar
        if (generadorDeltas != null) {
            generadorDeltas.cerrar();
        }
        
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
        if (comunicacion != null) {
            comunicacion.cerrarConexion();
        }
    }
}
