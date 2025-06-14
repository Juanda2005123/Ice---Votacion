package enrutamiento;

import config.ConfiguracionBroker;
import java.util.List;
import java.util.Random;

/**
 * Estrategia de enrutamiento para determinar a que destino enviar los votos.
 * Por el momento implementa enrutamiento aleatorio.
 */
public class EstrategiaEnrutamiento {
    
    private ConfiguracionBroker config;
    private Random random;
    
    public EstrategiaEnrutamiento(ConfiguracionBroker config) {
        this.config = config;
        this.random = new Random();
    }
    
    /**
     * Selecciona un destino de forma aleatoria entre los destinos activos
     * 
     * @return El destino seleccionado o null si no hay destinos activos
     */
    public ConfiguracionBroker.Destino seleccionarDestino() {
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
     * Obtiene estadisticas del enrutamiento
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
     * Verifica si hay destinos disponibles para enrutamiento
     */
    public boolean hayDestinosDisponibles() {
        return !config.getDestinosActivos().isEmpty();
    }
    
    /**
     * Obtiene el numero de destinos activos
     */
    public int getNumeroDestinosActivos() {
        return config.getDestinosActivos().size();
    }
}
