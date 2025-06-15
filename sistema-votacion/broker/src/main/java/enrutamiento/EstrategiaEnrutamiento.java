package enrutamiento;

import config.ConfiguracionBroker;
import java.util.List;
import java.util.Random;

/**
 * Estrategia de enrutamiento para determinar a que destino enviar los votos.
 * 
 * Esta clase es responsable de:
 * - Seleccionar destinos entre los disponibles segun la estrategia configurada
 * - Proporcionar estadisticas sobre el estado de los destinos
 * - Verificar disponibilidad de destinos para enrutamiento
 * 
 * Actualmente implementa enrutamiento aleatorio, pero puede extenderse
 * para soportar otras estrategias como round-robin, por prioridad, etc.
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-06-14
 */
public class EstrategiaEnrutamiento {
    
    private ConfiguracionBroker config;
    private Random random;
    
    /**
     * Constructor que inicializa la estrategia con la configuracion del broker.
     * 
     * @param config Configuracion del broker que contiene los destinos disponibles
     */
    public EstrategiaEnrutamiento(ConfiguracionBroker config) {
        this.config = config;
        this.random = new Random();
    }
    
    /**
     * Selecciona un destino de forma aleatoria entre los destinos activos.
     * 
     * @return El destino seleccionado o null si no hay destinos activos
     */    public ConfiguracionBroker.Destino seleccionarDestino() {
        List<ConfiguracionBroker.Destino> destinosActivos = config.getDestinosActivos();
        if (destinosActivos.isEmpty()) {
            return null;
        }
        
        // Seleccion aleatoria
        ConfiguracionBroker.Destino destinoSeleccionado = 
            destinosActivos.get(random.nextInt(destinosActivos.size()));
        
        return destinoSeleccionado;
    }
    
    /**
     * Obtiene estadisticas detalladas del enrutamiento y estado de destinos.
     * 
     * @return Cadena con estadisticas formateadas del enrutamiento
     */
    public String getEstadisticas() {
        List<ConfiguracionBroker.Destino> destinosActivos = config.getDestinosActivos();
        List<ConfiguracionBroker.Destino> todosDestinos = config.getTodosLosDestinos();
        
        StringBuilder stats = new StringBuilder();
        stats.append("=== ESTADISTICAS DE ENRUTAMIENTO ===\n");
        stats.append("Estrategia: ").append(config.getEstrategiaEnrutamiento()).append("\n");
        stats.append("Destinos totales: ").append(todosDestinos.size()).append("\n");
        stats.append("Destinos activos: ").append(destinosActivos.size()).append("\n");
        stats.append("Destinos inactivos: ").append(todosDestinos.size() - destinosActivos.size()).append("\n");
        
        stats.append("\nDestinos disponibles:\n");
        for (ConfiguracionBroker.Destino destino : todosDestinos) {
            stats.append("- ").append(destino.getId())
                 .append(" (").append(destino.getHost()).append(":").append(destino.getPuerto()).append(")")
                 .append(" - Estado: ").append(destino.isActivo() ? "ACTIVO" : "INACTIVO")
                 .append("\n");
        }
          return stats.toString();
    }
    
    /**
     * Verifica si hay destinos disponibles para enrutamiento.
     * 
     * @return true si hay al menos un destino activo, false en caso contrario
     */
    public boolean hayDestinosDisponibles() {
        return !config.getDestinosActivos().isEmpty();
    }
    
    /**
     * Obtiene el numero de destinos activos disponibles para enrutamiento.
     * 
     * @return Numero de destinos activos
     */
    public int getNumeroDestinosActivos() {
        return config.getDestinosActivos().size();
    }
}
