package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Maneja la configuración del broker cargada desde broker.properties
 */
public class ConfiguracionBroker {
    
    private Properties properties;
    private List<Destino> destinos;
    
    public ConfiguracionBroker(String rutaArchivo) {
        this.properties = new Properties();
        this.destinos = new ArrayList<>();
        cargarConfiguracion(rutaArchivo);
        cargarDestinos();
    }
    
    private void cargarConfiguracion(String rutaArchivo) {
        try (FileInputStream fis = new FileInputStream(rutaArchivo)) {
            properties.load(fis);
            System.out.println("Configuración cargada desde: " + rutaArchivo);
        } catch (IOException e) {
            throw new RuntimeException("Error cargando configuración: " + e.getMessage());
        }
    }
    
    private void cargarDestinos() {
        int cantidad = Integer.parseInt(properties.getProperty("destinos.cantidad", "0"));
        
        for (int i = 1; i <= cantidad; i++) {
            String id = properties.getProperty("destino." + i + ".id");
            String host = properties.getProperty("destino." + i + ".host");
            int puerto = Integer.parseInt(properties.getProperty("destino." + i + ".puerto"));
            boolean activo = Boolean.parseBoolean(properties.getProperty("destino." + i + ".activo", "true"));
            
            if (id != null && host != null) {
                destinos.add(new Destino(id, host, puerto, activo));
                System.out.println("Destino cargado: " + id + " -> " + host + ":" + puerto + " (activo: " + activo + ")");
            }
        }
    }
    
    // Getters para propiedades del broker
    public String getBrokerId() {
        return properties.getProperty("broker.id", "BROKER-DEFAULT");
    }
    
    public String getBrokerNombre() {
        return properties.getProperty("broker.nombre", "Broker Default");
    }
    
    public String getHost() {
        return properties.getProperty("broker.host", "localhost");
    }
    
    public int getPuerto() {
        return Integer.parseInt(properties.getProperty("broker.puerto", "9000"));
    }
    
    public int getTimeout() {
        return Integer.parseInt(properties.getProperty("broker.timeout", "5000"));
    }
    
    public int getReintentos() {
        return Integer.parseInt(properties.getProperty("enrutamiento.reintentos", "3"));
    }
    
    public int getEnrutamientoTimeout() {
        return Integer.parseInt(properties.getProperty("enrutamiento.timeout", "3000"));
    }
    
    public String getEstrategiaEnrutamiento() {
        return properties.getProperty("enrutamiento.estrategia", "ALEATORIO");
    }
    
    // Métodos para manejar destinos
    public List<Destino> getTodosLosDestinos() {
        return new ArrayList<>(destinos);
    }
    
    public List<Destino> getDestinosActivos() {
        return destinos.stream()
                .filter(Destino::isActivo)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    public Destino getDestinoPorId(String id) {
        return destinos.stream()
                .filter(d -> d.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Clase interna que representa un destino de reenvío
     */
    public static class Destino {
        private String id;
        private String host;
        private int puerto;
        private boolean activo;
        
        public Destino(String id, String host, int puerto, boolean activo) {
            this.id = id;
            this.host = host;
            this.puerto = puerto;
            this.activo = activo;
        }
        
        // Getters
        public String getId() { return id; }
        public String getHost() { return host; }
        public int getPuerto() { return puerto; }
        public boolean isActivo() { return activo; }
        
        // Setters
        public void setActivo(boolean activo) { this.activo = activo; }
        
        @Override
        public String toString() {
            return String.format("Destino{id='%s', host='%s', puerto=%d, activo=%s}", 
                               id, host, puerto, activo);
        }
    }
}
