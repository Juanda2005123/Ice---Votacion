package deltas;

import VotingSystem.DeltaConteo;
import config.ConfiguracionDepartamento;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Generador de deltas consolidadas departamentales con control de umbral.
 * Maneja el envío automático basado en número de votos o tiempo transcurrido.
 * 
 * @author Sistema de Votacion
 * @version 2.0 - Map-Reduce Level 2
 * @since 2025-06-15
 */
public class GeneradorDeltas {
    
    private final RepositorioDeltas repositorio;
    private final ConfiguracionDepartamento config;
    private final Consumer<DeltaConteo> enviarDelta;
    private final ScheduledExecutorService scheduler;
    
    private volatile long ultimoEnvio;
    
    /**
     * Constructor del generador de deltas consolidadas departamentales.
     * 
     * @param repositorio Repositorio de deltas consolidadas
     * @param config Configuración con umbrales
     * @param enviarDelta Función callback para enviar delta
     */
    public GeneradorDeltas(RepositorioDeltas repositorio, ConfiguracionDepartamento config, 
                          Consumer<DeltaConteo> enviarDelta) {
        this.repositorio = repositorio;
        this.config = config;
        this.enviarDelta = enviarDelta;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.ultimoEnvio = System.currentTimeMillis();
        
        iniciarScheduler();
    }
    
    /**
     * Inicia el scheduler para envío automático por tiempo.
     */
    private void iniciarScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
            verificarEnvioPorTiempo();
        }, config.getDeltaUmbralTiempo(), config.getDeltaUmbralTiempo(), TimeUnit.MILLISECONDS);
    }
    
    /**
     * Procesa un delta de lugar y verifica si debe enviar consolidado departamental.
     * 
     * @param delta Delta recibido de lugar de votación
     */
    public void procesarDelta(DeltaConteo delta) {
        // MAP PHASE: Consolidar delta en repositorio
        repositorio.consolidarDelta(delta);
        
        // Verificar umbral de votos
        if (repositorio.getTotalVotosConsolidados() >= config.getDeltaUmbralVotos()) {
            enviarDeltaConsolidado();
        }
    }
    
    /**
     * Verifica si debe enviar por umbral de tiempo.
     */
    private void verificarEnvioPorTiempo() {
        long tiempoTranscurrido = System.currentTimeMillis() - ultimoEnvio;
        
        if (tiempoTranscurrido >= config.getDeltaUmbralTiempo() && !repositorio.estaVacio()) {
            enviarDeltaConsolidado();
        }
    }
    
    /**
     * REDUCE PHASE: Envía delta consolidado departamental y limpia contador.
     */
    private synchronized void enviarDeltaConsolidado() {
        if (repositorio.estaVacio()) {
            return; // No hay nada que enviar
        }
        
        // Crear delta consolidado departamental
        DeltaConteo deltaConsolidado = repositorio.crearDeltaConsolidado();
        
        // Enviar delta consolidado al servidor central
        enviarDelta.accept(deltaConsolidado);
        
        // RESET: Limpiar contador para próximo ciclo
        repositorio.limpiarContador();
        ultimoEnvio = System.currentTimeMillis();
    }
    
    /**
     * Fuerza el envío de delta consolidado actual.
     * Usado al cerrar el sistema.
     */
    public void forzarEnvio() {
        enviarDeltaConsolidado();
    }
    
    /**
     * Cierra el generador y envía último delta si existe.
     */
    public void cerrar() {
        // Enviar último delta antes de cerrar
        forzarEnvio();
        
        // Cerrar scheduler
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
}
