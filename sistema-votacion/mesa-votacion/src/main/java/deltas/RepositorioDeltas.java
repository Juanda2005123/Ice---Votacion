package deltas;

import model.Voto;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Repositorio para mantener conteos absolutos de votos por mesa.
 * 
 * Esta clase es responsable de:
 * - Mantener el conteo TOTAL de votos por candidato en esta mesa (conteo absoluto)
 * - Guardar todos los votos individuales para auditoría
 * - Proporcionar estadísticas de votación por mesa
 * - Thread-safe para operaciones concurrentes
 * 
 * DIFERENCIA CON GeneradorDeltas:
 * - RepositorioDeltas: Conteo ABSOLUTO total por mesa (nunca se resetea)
 * - GeneradorDeltas: Conteo INCREMENTAL desde último envío (se resetea)
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-06-15
 */
public class RepositorioDeltas {
    
    private final String mesaId;
    
    // Conteo ABSOLUTO por candidato (total de votos desde inicio)
    private final Map<Integer, AtomicInteger> conteoAbsoluto;
    
    // Almacenamiento de todos los votos para auditoría
    private final Map<Integer, Voto> todosLosVotos;
    
    // Contador secuencial de votos
    private final AtomicInteger contadorTotalVotos;
    
    /**
     * Constructor que inicializa el repositorio para una mesa específica.
     * 
     * @param mesaId Identificador de la mesa de votación
     */
    public RepositorioDeltas(String mesaId) {
        this.mesaId = mesaId;
        this.conteoAbsoluto = new ConcurrentHashMap<>();
        this.todosLosVotos = new ConcurrentHashMap<>();
        this.contadorTotalVotos = new AtomicInteger(0);
    }
    
    /**
     * Registra un voto en el repositorio absoluto.
     * Incrementa el conteo total del candidato y guarda el voto completo.
     * 
     * @param voto Voto a registrar
     * @return Número total de votos registrados hasta ahora
     */    public int registrarVoto(Voto voto) {
        Integer candidatoId = voto.getCandidato().getId();
        
        // Incrementar conteo absoluto del candidato
        conteoAbsoluto.computeIfAbsent(candidatoId, k -> new AtomicInteger(0))
                     .incrementAndGet();
        
        // Guardar voto completo para auditoría
        todosLosVotos.put(voto.getId(), voto);
        
        // Incrementar contador total
        int totalVotos = contadorTotalVotos.incrementAndGet();
        
        return totalVotos;
    }
    
    /**
     * Obtiene el conteo absoluto total por candidato.
     * Nunca se resetea - siempre refleja el total desde el inicio.
     * 
     * @return Mapa con candidatoId -> total de votos
     */
    public Map<Integer, Integer> getConteoAbsoluto() {
        Map<Integer, Integer> conteo = new ConcurrentHashMap<>();
        
        for (Map.Entry<Integer, AtomicInteger> entry : conteoAbsoluto.entrySet()) {
            conteo.put(entry.getKey(), entry.getValue().get());
        }
        
        return conteo;
    }
    
    /**
     * Obtiene el total de votos registrados en esta mesa.
     * 
     * @return Número total de votos desde el inicio
     */
    public int getTotalVotos() {
        return contadorTotalVotos.get();
    }
    
    /**
     * Obtiene el conteo de votos para un candidato específico.
     * 
     * @param candidatoId ID del candidato
     * @return Número de votos del candidato, 0 si no ha recibido votos
     */
    public int getVotosCandidato(Integer candidatoId) {
        AtomicInteger contador = conteoAbsoluto.get(candidatoId);
        return contador != null ? contador.get() : 0;
    }
    
    /**
     * Obtiene un voto específico por su ID para auditoría.
     * 
     * @param votoId ID del voto
     * @return Voto correspondiente o null si no existe
     */
    public Voto getVotoPorId(Integer votoId) {
        return todosLosVotos.get(votoId);
    }
    
    /**
     * Obtiene el número de candidatos que han recibido al menos un voto.
     * 
     * @return Número de candidatos con votos
     */
    public int getCandidatosConVotos() {
        return conteoAbsoluto.size();
    }
    
    /**
     * Genera estadísticas completas de la mesa para reportes.
     * 
     * @return String con estadísticas detalladas
     */
    public String getEstadisticasMesa() {
        StringBuilder stats = new StringBuilder();
        stats.append("=== ESTADISTICAS MESA ").append(mesaId).append(" ===\n");
        stats.append("Total votos: ").append(getTotalVotos()).append("\n");
        stats.append("Candidatos con votos: ").append(getCandidatosConVotos()).append("\n");
        stats.append("Distribución por candidato:\n");
        
        for (Map.Entry<Integer, AtomicInteger> entry : conteoAbsoluto.entrySet()) {
            stats.append("- Candidato ").append(entry.getKey())
                 .append(": ").append(entry.getValue().get()).append(" votos\n");
        }
        
        return stats.toString();
    }
    
    /**
     * Verifica la integridad de los datos del repositorio.
     * 
     * @return true si los datos son consistentes
     */
    public boolean verificarIntegridad() {
        // Verificar que la suma de votos por candidato = total de votos
        int sumaConteos = conteoAbsoluto.values().stream()
                                       .mapToInt(AtomicInteger::get)
                                       .sum();
        
        boolean integridadConteos = (sumaConteos == contadorTotalVotos.get());
        boolean integridadVotos = (todosLosVotos.size() == contadorTotalVotos.get());
        
        if (!integridadConteos || !integridadVotos) {
            System.err.println("[REPO] ERROR DE INTEGRIDAD - Suma conteos: " + sumaConteos + 
                             ", Total: " + contadorTotalVotos.get() + 
                             ", Votos guardados: " + todosLosVotos.size());
        }
        
        return integridadConteos && integridadVotos;
    }
}
