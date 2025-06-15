package enrutamiento;

import config.ConfiguracionBroker;
import comunicacion.ServicioVerificacionConectividad;
import java.util.List;
import java.util.ArrayList;
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
    private ServicioVerificacionConectividad verificador;
    
    /**
     * Constructor que inicializa la estrategia con la configuracion del broker.
     * 
     * @param config Configuracion del broker que contiene los destinos disponibles
     */
    public EstrategiaEnrutamiento(ConfiguracionBroker config) {
        this.config = config;
        this.random = new Random();
        this.verificador = new ServicioVerificacionConectividad();
    }
      /**
     * Selecciona un destino de forma aleatoria entre los destinos REALMENTE CONECTADOS.
     * Verifica la conectividad en tiempo real antes de seleccionar.
     * 
     * @return El destino seleccionado y conectado, o null si no hay destinos disponibles
     */    
    public ConfiguracionBroker.Destino seleccionarDestino() {
        List<ConfiguracionBroker.Destino> destinosActivos = config.getDestinosActivos();
        if (destinosActivos.isEmpty()) {
            return null;
        }
        
        // NUEVA LOGICA: Filtrar solo destinos realmente conectados
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
        
        // Seleccion aleatoria entre destinos REALMENTE conectados
        ConfiguracionBroker.Destino destinoSeleccionado = 
            destinosConectados.get(random.nextInt(destinosConectados.size()));
        
        return destinoSeleccionado;
    }    /**
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
     * Cierra el verificador de conectividad y libera recursos.
     */
    public void cerrar() {
        if (verificador != null) {
            verificador.cerrar();
        }
    }
}
