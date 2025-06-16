package controller;

import VotingSystem.DeltaConteo;
import config.ConfiguracionServidor;
import consolidacion.ConsolidadorNacional;
import consolidacion.GeneradorCSV;
import gestion.GestorCandidatos;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Controlador principal del servidor central con Reduce Final.
 * 
 * Este controlador implementa el destino final del sistema Map-Reduce:
 * - THREAD POOL: Procesa deltas departamentales concurrentemente
 * - CONTEO NACIONAL: Consolidación final en ConcurrentHashMap
 * - GENERACIÓN CSV: Reporte final al cerrar el servidor
 * 
 * @author Sistema de Votacion
 * @version 3.0 - Reduce Final
 * @since 2025-06-15
 */
public class ServidorController {    private ExecutorService threadPool;
    private ConsolidadorNacional consolidador;
    private GeneradorCSV generadorCSV;
    private GestorCandidatos gestorCandidatos;    /**
     * Constructor que inicializa el controlador con Reduce Final.
     * 
     * @param config Configuracion del servidor central con parametros thread pool
     * @param gestorCandidatos Gestor de candidatos para nombres en CSV final
     */
    public ServidorController(ConfiguracionServidor config, GestorCandidatos gestorCandidatos) {        
        this.gestorCandidatos = gestorCandidatos;
        
        // Inicializar Thread Pool para procesamiento concurrente
        int poolSize = config.getThreadPoolSize();
        this.threadPool = Executors.newFixedThreadPool(poolSize);
        
        // Inicializar consolidador nacional y generador CSV (con gestor de candidatos)
        this.consolidador = new ConsolidadorNacional(config.getServidorId());
        this.generadorCSV = new GeneradorCSV(gestorCandidatos);
    }
    /**
     * REDUCE FINAL: Procesa un delta departamental usando Thread Pool.
     * Cada hilo ejecuta la consolidación nacional en paralelo.
     * 
     * @param delta Delta departamental recibido
     * @return true si el delta fue enviado para procesamiento
     */
    public boolean procesarDelta(DeltaConteo delta) {
        try {
            // Enviar para procesamiento asíncrono en Thread Pool (REDUCE FINAL)
            threadPool.submit(() -> {
                consolidador.consolidarDelta(delta);
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Obtiene el conteo nacional actual.
     * 
     * @return Mapa con candidatoId -> totalVotos
     */
    public java.util.Map<Integer, Integer> obtenerConteoNacional() {
        return consolidador.obtenerConteoNacional();
    }
    
    /**
     * Obtiene el total de votos a nivel nacional.
     * 
     * @return Total de votos consolidados
     */
    public int obtenerTotalVotos() {
        return consolidador.obtenerTotalVotos();
    }
    
    /**
     * Verifica si hay votos registrados.
     * 
     * @return true si hay votos, false en caso contrario
     */
    public boolean hayVotos() {
        return consolidador.hayVotos();
    }
    
    /**
     * Valida un voto - NO IMPLEMENTADO EN SERVIDOR CENTRAL.
     * El servidor central solo recibe deltas consolidados.
     * 
     * @param documento Documento del votante como String
     * @param candidatoId ID del candidato elegido
     * @return No retorna, lanza excepcion
     * @throws UnsupportedOperationException Siempre, ya que no se utiliza en servidor central
     */
    public int validarVoto(String documento, Integer candidatoId) {
        throw new UnsupportedOperationException("La validacion de votantes no se implementa en el servidor central. Solo se procesan deltas consolidados.");
    }
    
    /**
     * Genera el reporte CSV final y cierra todos los recursos.
     * Debe llamarse al finalizar el uso del controlador.
     */
    public void cerrarYGenerarReporte() {
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
        
        // Generar reporte CSV final
        if (consolidador.hayVotos()) {
            generadorCSV.generarReporte(consolidador.obtenerConteoNacional());
        } else {
            generadorCSV.generarReporteVacio();
        }
        
        System.out.println("Servidor central cerrado correctamente.");
    }
}
