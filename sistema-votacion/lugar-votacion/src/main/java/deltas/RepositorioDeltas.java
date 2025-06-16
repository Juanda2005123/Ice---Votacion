package deltas;

import VotingSystem.DeltaConteo;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Repositorio para consolidar deltas usando Map-Reduce en Lugar de Votación.
 * Maneja el estado compartido de conteos consolidados de múltiples mesas.
 * 
 * MAP PHASE: Cada hilo suma deltas de mesa al contador consolidado
 * REDUCE PHASE: Crea delta consolidado cuando alcanza umbral
 * 
 * @author Sistema de Votacion
 * @version 2.0 - Map-Reduce Level 1
 * @since 2025-06-15
 */
public class RepositorioDeltas {
    
    private final ConcurrentHashMap<Integer, AtomicInteger> conteoConsolidado;
    private final AtomicInteger totalVotos;
    private final String nodoId;
    private final AtomicInteger contadorDeltas;
    
    /**
     * Constructor del repositorio de deltas consolidadas.
     * 
     * @param nodoId ID del nodo lugar de votación
     */    public RepositorioDeltas(String nodoId) {
        this.nodoId = nodoId;
        this.conteoConsolidado = new ConcurrentHashMap<>();
        this.totalVotos = new AtomicInteger(0);
        this.contadorDeltas = new AtomicInteger(0);
    }
    
    /**
     * MAP PHASE: Consolida un delta de mesa en el contador compartido.
     * Thread-safe para procesamiento paralelo.
     * 
     * @param delta Delta recibido de una mesa de votación
     */
    public synchronized void consolidarDelta(DeltaConteo delta) {
        // Sumar cada candidato del delta al conteo consolidado
        for (var entry : delta.conteo.entrySet()) {
            int candidatoId = entry.getKey();
            int votos = entry.getValue();
            
            conteoConsolidado.computeIfAbsent(candidatoId, k -> new AtomicInteger(0))
                            .addAndGet(votos);
        }
        
        // Actualizar total de votos consolidados
        totalVotos.addAndGet(delta.totalVotos);
    }
    
    /**
     * REDUCE PHASE: Crea delta consolidado del estado actual.
     * 
     * @return Delta consolidado listo para enviar
     */    public synchronized DeltaConteo crearDeltaConsolidado() {
        DeltaConteo deltaConsolidado = new DeltaConteo();
        deltaConsolidado.nodoId = nodoId;
        deltaConsolidado.deltaId = nodoId + "-" + contadorDeltas.incrementAndGet();
        deltaConsolidado.timestamp = System.currentTimeMillis();
        deltaConsolidado.totalVotos = totalVotos.get();
        
        // Convertir ConcurrentHashMap a Map normal para Ice
        deltaConsolidado.conteo = new java.util.HashMap<>();
        for (var entry : conteoConsolidado.entrySet()) {
            deltaConsolidado.conteo.put(entry.getKey(), entry.getValue().get());
        }
        
        return deltaConsolidado;
    }
    
    /**
     * RESET: Limpia el contador después de enviar delta consolidado.
     * Prepara para el próximo ciclo de consolidación.
     */
    public synchronized void limpiarContador() {
        conteoConsolidado.clear();
        totalVotos.set(0);
    }
    
    /**
     * Obtiene el total de votos consolidados actuales.
     * 
     * @return Total de votos consolidados
     */
    public int getTotalVotosConsolidados() {
        return totalVotos.get();
    }
    
    /**
     * Verifica si el contador está vacío.
     * 
     * @return true si no hay votos consolidados
     */
    public boolean estaVacio() {
        return totalVotos.get() == 0;
    }
}
