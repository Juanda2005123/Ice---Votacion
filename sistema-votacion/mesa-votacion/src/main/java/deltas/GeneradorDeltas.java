package deltas;

import VotingSystem.*;
import config.ConfiguracionMesaVotacion;
import model.Candidato;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generador de deltas para Map-Reduce - Mesa de Votación.
 * 
 * Esta clase es responsable de:
 * - Mantener conteo incremental por candidato desde el último envío
 * - Detectar cuándo se alcanzan los umbrales (votos o tiempo) para enviar deltas
 * - Generar objetos DeltaConteo para envío al broker
 * - Resetear conteos después de cada envío
 * 
 * Implementa patrón Producer en arquitectura Map-Reduce:
 * - Acumula votos localmente (buffer)
 * - Envía deltas cuando alcanza umbrales configurados
 * - Mantiene trazabilidad con nodoId y timestamps
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-06-15
 */
public class GeneradorDeltas {
    
    private final ConfiguracionMesaVotacion config;
    private final String nodoId;
    
    // Buffer de deltas: candidatoId -> cantidad de votos desde último envío
    private final Map<Integer, Integer> bufferDeltas;
    
    // Control de umbrales
    private int contadorVotosBuffer;
    private long timestampUltimoEnvio;
    
    /**
     * Constructor que inicializa el generador con la configuración de la mesa.
     * 
     * @param config Configuración de la mesa con umbrales de deltas
     */
    public GeneradorDeltas(ConfiguracionMesaVotacion config) {
        this.config = config;
        this.nodoId = config.getMesaId();
        
        // Usar ConcurrentHashMap para thread-safety futuro
        this.bufferDeltas = new ConcurrentHashMap<>();
        this.contadorVotosBuffer = 0;
        this.timestampUltimoEnvio = System.currentTimeMillis();
    }
    
    /**
     * Registra un voto en el buffer de deltas.
     * Incrementa el contador para el candidato especificado.
     * 
     * @param candidato Candidato que recibió el voto
     */    public synchronized void registrarVoto(Candidato candidato) {
        Integer candidatoId = candidato.getId();
        
        // Incrementar contador del candidato en el buffer
        bufferDeltas.merge(candidatoId, 1, Integer::sum);
        contadorVotosBuffer++;
    }
    
    /**
     * Verifica si se debe enviar un delta basado en los umbrales configurados.
     * Evalúa tanto umbral de votos como umbral de tiempo.
     * 
     * @return true si se debe enviar delta, false en caso contrario
     */    public synchronized boolean debeEnviarDelta() {
        // Verificar umbral de votos
        if (contadorVotosBuffer >= config.getDeltaUmbralVotos()) {
            return true;
        }
        
        // Verificar umbral de tiempo
        long tiempoTranscurrido = System.currentTimeMillis() - timestampUltimoEnvio;
        if (tiempoTranscurrido >= config.getDeltaUmbralTiempo()) {
            return true;
        }
        
        return false;
    }/**
     * Genera un objeto DeltaConteo con el buffer actual.
     * Incluye información de nodo, timestamp y conteos incrementales.
     * 
     * @return DeltaConteo listo para envío, o null si no hay votos en buffer
     */    public synchronized DeltaConteo generarDelta() {
        // Si no hay votos en buffer, no generar delta
        if (contadorVotosBuffer == 0 || bufferDeltas.isEmpty()) {
            return null;
        }
        
        // Crear mapa de conteos para Ice (MapConteoVotos es Map<Integer, Integer>)
        Map<Integer, Integer> conteoIce = new HashMap<>(bufferDeltas);
        
        // Crear objeto DeltaConteo
        DeltaConteo delta = new DeltaConteo();
        delta.nodoId = nodoId;
        delta.timestamp = System.currentTimeMillis();
        delta.totalVotos = contadorVotosBuffer;
        delta.conteo = conteoIce;
        
        return delta;
    }
    
    /**
     * Resetea el buffer después de enviar un delta exitosamente.
     * Limpia contadores y actualiza timestamp del último envío.
     */    public synchronized void resetearBuffer() {
        bufferDeltas.clear();
        contadorVotosBuffer = 0;
        timestampUltimoEnvio = System.currentTimeMillis();
    }
    
    /**
     * Obtiene estadísticas del buffer actual para debugging.
     * 
     * @return String con información del estado actual del buffer
     */
    public synchronized String getEstadisticasBuffer() {
        long tiempoTranscurrido = System.currentTimeMillis() - timestampUltimoEnvio;
        
        return String.format("Buffer Delta - Votos: %d/%d, Tiempo: %dms/%dms, Candidatos: %d",
                           contadorVotosBuffer, config.getDeltaUmbralVotos(),
                           tiempoTranscurrido, config.getDeltaUmbralTiempo(),
                           bufferDeltas.size());
    }
    
    /**
     * Verifica si hay votos pendientes en el buffer.
     * 
     * @return true si hay votos no enviados en el buffer
     */
    public synchronized boolean hayVotosPendientes() {
        return contadorVotosBuffer > 0;
    }
}
