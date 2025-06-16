package consolidacion;

import VotingSystem.DeltaConteo;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;

/**
 * Repositorio nacional de consolidación final para el servidor central.
 * 
 * Esta clase implementa el REDUCE FINAL del sistema Map-Reduce:
 * - Consolida deltas departamentales en conteo nacional
 * - Thread-safe usando ConcurrentHashMap y AtomicInteger
 * - Proporciona acceso al conteo nacional final
 * 
 * @author Sistema de Votacion
 * @version 3.0 - Reduce Final
 * @since 2025-06-15
 */
public class ConsolidadorNacional {
    
    private final String servidorId;
    private final ConcurrentHashMap<Integer, AtomicInteger> conteoNacional;
    
    // Protección contra duplicados: set thread-safe para rastrear IDs de deltas procesados
    private final ConcurrentHashMap<String, Boolean> deltasProcesados;

    /**
     * Constructor que inicializa el consolidador nacional.
     * 
     * @param servidorId ID del servidor central
     */
    public ConsolidadorNacional(String servidorId) {
        this.servidorId = servidorId;
        this.conteoNacional = new ConcurrentHashMap<>();
        this.deltasProcesados = new ConcurrentHashMap<>();
    }    /**
     * REDUCE FINAL: Consolida un delta departamental en el conteo nacional.
     * Esta operación es thread-safe y atómica.
     * Incluye protección contra procesamiento duplicado de deltas.
     * 
     * @param delta Delta departamental a consolidar
     * @return true si el delta fue procesado, false si ya había sido procesado antes
     */
    public boolean consolidarDelta(DeltaConteo delta) {
        // Verificar si el delta ya fue procesado usando su ID único
        if (deltasProcesados.putIfAbsent(delta.deltaId, Boolean.TRUE) != null) {
            // Delta duplicado - ya fue procesado
            System.out.println("ADVERTENCIA: Delta duplicado detectado: " + delta.deltaId);
            return false;
        }
        
        // Delta nuevo - procesar normalmente
        for (Map.Entry<Integer, Integer> entry : delta.conteo.entrySet()) {
            Integer candidatoId = entry.getKey();
            Integer votos = entry.getValue();
            
            // Consolidación atómica en el conteo nacional
            conteoNacional.computeIfAbsent(candidatoId, k -> new AtomicInteger(0))
                         .addAndGet(votos);
        }
        
        return true;
    }
    
    /**
     * Obtiene el conteo nacional actual.
     * 
     * @return Mapa inmutable con el conteo nacional
     */
    public Map<Integer, Integer> obtenerConteoNacional() {
        Map<Integer, Integer> resultado = new ConcurrentHashMap<>();
        for (Map.Entry<Integer, AtomicInteger> entry : conteoNacional.entrySet()) {
            resultado.put(entry.getKey(), entry.getValue().get());
        }
        return resultado;
    }
    
    /**
     * Obtiene el total de votos a nivel nacional.
     * 
     * @return Total de votos consolidados
     */
    public int obtenerTotalVotos() {
        return conteoNacional.values().stream()
                           .mapToInt(AtomicInteger::get)
                           .sum();
    }
    
    /**
     * Verifica si hay votos registrados.
     * 
     * @return true si hay votos, false en caso contrario
     */
    public boolean hayVotos() {
        return !conteoNacional.isEmpty() && obtenerTotalVotos() > 0;
    }
    
    /**
     * Reinicia el conteo nacional (para testing).
     */
    public void reiniciar() {
        conteoNacional.clear();
        deltasProcesados.clear();
    }
}
