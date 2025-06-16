package controller;

import VotingSystem.DeltaConteo;
import config.ConfiguracionDepartamento;
import comunicacion.ServicioComunicacionDepartamento;
import comunicacion.ServicioVerificacionConectividad;
import deltas.RepositorioDeltas;
import deltas.GeneradorDeltas;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Controlador principal del departamento con Map-Reduce Level 2.
 * 
 * Este controlador implementa el segundo nivel de consolidación Map-Reduce:
 * - MAP PHASE: Thread Pool recibe deltas de lugares, los consolida en paralelo
 * - REDUCE PHASE: Crea y envía deltas departamentales al servidor central
 * - ESTADO COMPARTIDO: ConcurrentHashMap thread-safe para conteos departamentales
 * 
 * @author Sistema de Votacion
 * @version 2.0 - Map-Reduce Level 2
 * @since 2025-06-15
 */
public class DepartamentoController {
    
    private ServicioComunicacionDepartamento comunicacion;
    private ServicioVerificacionConectividad verificador;
    private ConfiguracionDepartamento config;
    private ExecutorService threadPool;
    private RepositorioDeltas repositorioDeltas;
    private GeneradorDeltas generadorDeltas;

    /**
     * Constructor que inicializa el controlador con Map-Reduce departamental.
     * 
     * @param config Configuracion del departamento con parametros Map-Reduce
     */
    public DepartamentoController(ConfiguracionDepartamento config) {        
        this.config = config;
        this.comunicacion = new ServicioComunicacionDepartamento(config);
        this.verificador = new ServicioVerificacionConectividad();
        
        // Inicializar Thread Pool para MAP PHASE departamental
        int poolSize = config.getThreadPoolSize();
        this.threadPool = Executors.newFixedThreadPool(poolSize);
        
        // Inicializar sistema Map-Reduce departamental
        this.repositorioDeltas = new RepositorioDeltas(config.getDepartamentoId());
        this.generadorDeltas = new GeneradorDeltas(
            repositorioDeltas, 
            config, 
            this::enviarDeltaConsolidado
        );
    }      /**
     * Verifica la conectividad con el servidor central al iniciar el departamento.
     */
    public void verificarConectividadInicial() {
        boolean conectado = verificador.verificarConectividad(config);
        if (!conectado) {
            // Solo log de error crítico
        }
    }
    
    /**
     * MAP PHASE: Procesa un delta de lugar usando Thread Pool departamental.
     * Cada hilo ejecuta la consolidación departamental en paralelo.
     * 
     * @param delta Delta recibido de lugar de votación
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
     * CALLBACK: Envía delta consolidado departamental al servidor central.
     * Llamado por GeneradorDeltas cuando alcanza umbral.
     * 
     * @param deltaConsolidado Delta consolidado departamental listo para enviar
     */
    private void enviarDeltaConsolidado(DeltaConteo deltaConsolidado) {
        comunicacion.reenviarDelta(deltaConsolidado);
    }       
    /**
     * Valida un voto - NO IMPLEMENTADO EN DEPARTAMENTO.
     * El departamento no realiza validaciones de ciudadanos, solo consolidación de deltas.
     * 
     * @param documento Documento del votante como String
     * @param candidatoId ID del candidato elegido
     * @return No retorna, lanza excepcion
     * @throws UnsupportedOperationException Siempre, ya que no se utiliza en departamento
     */
    public int validarVoto(String documento, Integer candidatoId) {
        throw new UnsupportedOperationException("La validacion de ciudadanos no se implementa en el departamento. Los deltas se consolidan y reenvian al servidor central.");
    }
    
    /**
     * Verifica conectividad con el servidor central.
     * Util para diagnostico y monitoreo del estado de la conexion.
     */
    public void verificarEstadoDestinos() {
        verificador.verificarConectividadDestino(config);
    }
    
    /**
     * Verifica conectividad con el servidor central.
     * Metodo de compatibilidad para verificacion directa.
     * 
     * @return true si la conexion es exitosa, false en caso contrario
     */
    public boolean verificarConectividadServidor() {
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
