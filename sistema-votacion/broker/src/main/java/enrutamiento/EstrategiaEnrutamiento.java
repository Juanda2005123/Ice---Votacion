package enrutamiento;

import config.ConfiguracionBroker;
import comunicacion.ServicioVerificacionConectividad;
import java.util.List;
import java.util.ArrayList;

/**
 * Estrategia de enrutamiento para determinar a que destino enviar los votos.
 * 
 * Esta clase es responsable de:
 * - Seleccionar destinos entre los disponibles segun la estrategia configurada
 * - Implementar Load Balancing LRU (Least Recently Used) para distribucion equitativa
 * - Verificar disponibilidad de destinos para enrutamiento
 * - Mantener estadisticas sobre el estado de los destinos
 * 
 * Utiliza estrategia LRU: selecciona el destino que hace mas tiempo no se ha usado,
 * garantizando distribucion equitativa en entornos LAN con nodos identicos.
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-06-14
 */
public class EstrategiaEnrutamiento {    
    private ConfiguracionBroker config;
    private ServicioVerificacionConectividad verificador;
    
    /**
     * Constructor que inicializa la estrategia con la configuracion del broker.
     * 
     * @param config Configuracion del broker que contiene los destinos disponibles
     */
    public EstrategiaEnrutamiento(ConfiguracionBroker config) {
        this.config = config;
        this.verificador = new ServicioVerificacionConectividad();
    }      /**
     * Selecciona un destino usando estrategia LRU (Least Recently Used).
     * Verifica la conectividad en tiempo real antes de seleccionar.
     * Selecciona el destino conectado que hace mas tiempo no se ha usado.
     * 
     * @return El destino seleccionado y conectado, o null si no hay destinos disponibles
     */    
    public ConfiguracionBroker.Destino seleccionarDestino() {
        List<ConfiguracionBroker.Destino> destinosActivos = config.getDestinosActivos();
        if (destinosActivos.isEmpty()) {
            return null;
        }
        
        // PASO 1: Filtrar solo destinos realmente conectados (conservamos esta funcionalidad)
        List<ConfiguracionBroker.Destino> destinosConectados = new ArrayList<>();
        
        for (ConfiguracionBroker.Destino destino : destinosActivos) {
            if (verificador.verificarConectividad(destino)) {
                destinosConectados.add(destino);
            }
        }
          // Si no hay destinos conectados, retornar null
        if (destinosConectados.isEmpty()) {
            return null;
        }
        
        // PASO 2: NUEVA LOGICA LRU - Seleccionar el que hace mas tiempo no se usa
        ConfiguracionBroker.Destino destinoSeleccionado = destinosConectados.get(0);
        long tiempoMasAntiguo = destinoSeleccionado.getUltimoUso();
        
        for (ConfiguracionBroker.Destino destino : destinosConectados) {
            if (destino.getUltimoUso() < tiempoMasAntiguo) {
                tiempoMasAntiguo = destino.getUltimoUso();
                destinoSeleccionado = destino;
            }
        }
        
        // PASO 3: Marcar el destino seleccionado como usado AHORA
        destinoSeleccionado.marcarComoUsado();
        
        return destinoSeleccionado;
    }/**
     * Verifica si hay destinos disponibles para enrutamiento (realmente conectados).
     * 
     * @return true si hay al menos un destino activo Y conectado, false en caso contrario
     */
    public boolean hayDestinosDisponibles() {
        List<ConfiguracionBroker.Destino> destinosActivos = config.getDestinosActivos();
        
        for (ConfiguracionBroker.Destino destino : destinosActivos) {
            if (verificador.verificarConectividad(destino)) {
                return true; // Al menos uno esta conectado
            }
        }
        
        return false; // Ningun destino esta realmente conectado
    }
      /**
     * Obtiene el numero de destinos realmente conectados (no solo activos en config).
     * 
     * @return Numero de destinos que estan realmente conectados
     */
    public int getNumeroDestinosActivos() {
        List<ConfiguracionBroker.Destino> destinosActivos = config.getDestinosActivos();
        int conectados = 0;
        
        for (ConfiguracionBroker.Destino destino : destinosActivos) {
            if (verificador.verificarConectividad(destino)) {
                conectados++;
            }
        }
        
        return conectados;
    }
    
    /**
     * Obtiene estadisticas de uso LRU para todos los destinos.
     * Util para monitoreo y debugging del load balancing.
     * 
     * @return String con estadisticas de uso de cada destino
     */
    public String getEstadisticasLRU() {
        StringBuilder stats = new StringBuilder();
        stats.append("=== ESTADISTICAS LOAD BALANCING LRU ===\n");
        
        List<ConfiguracionBroker.Destino> destinos = config.getTodosLosDestinos();
        long tiempoActual = System.currentTimeMillis();
        
        for (ConfiguracionBroker.Destino destino : destinos) {
            long ultimoUso = destino.getUltimoUso();
            String estadoUso;
            
            if (ultimoUso == 0) {
                estadoUso = "NUNCA USADO";
            } else {
                long segundosTranscurridos = (tiempoActual - ultimoUso) / 1000;
                estadoUso = segundosTranscurridos + "s atras";
            }
            
            boolean conectado = verificador.verificarConectividad(destino);
            stats.append(String.format("- %s: %s (conectado=%s)\n", 
                destino.getId(), estadoUso, conectado));
        }
        
        return stats.toString();
    }
    
    /**
     * Cierra el verificador de conectividad y libera recursos.
     */
    public void cerrar() {
        if (verificador != null) {
            verificador.cerrar();
        }
    }
}
