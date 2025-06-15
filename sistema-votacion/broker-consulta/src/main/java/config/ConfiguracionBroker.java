package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ConfiguracionBroker {
    private final Properties properties = new Properties();
    private final List<Destino> destinos = new ArrayList<>();

    public ConfiguracionBroker(String rutaArchivo) {
        try (FileInputStream fis = new FileInputStream(rutaArchivo)) {
            properties.load(fis);
            cargarDestinos();
        } catch (IOException e) {
            throw new RuntimeException("Error cargando configuracion: " + e.getMessage());
        }
    }

    public String getPropiedad(String clave, String valorPorDefecto) {
        return properties.getProperty(clave, valorPorDefecto);
    }

    private void cargarDestinos() {
        int cantidad = Integer.parseInt(properties.getProperty("destinos.cantidad", "0"));
        for (int i = 1; i <= cantidad; i++) {
            String id = properties.getProperty("destino." + i + ".id");
            String host = properties.getProperty("destino." + i + ".host");
            int puerto = Integer.parseInt(properties.getProperty("destino." + i + ".puerto"));
            boolean activo = Boolean.parseBoolean(properties.getProperty("destino." + i + ".activo", "true"));
            String tipo = properties.getProperty("destino." + i + ".tipo", "consulta");
            destinos.add(new Destino(id, host, puerto, activo, tipo));
        }
    }

    public String getBrokerId() { return properties.getProperty("nodo.id", "BROKER-CONSULTAS"); }
    public String getBrokerNombre() { return properties.getProperty("nodo.nombre", "Broker Consultas"); }
    public String getHost() { return properties.getProperty("broker.host", "localhost"); }
    public int getPuerto() { return Integer.parseInt(properties.getProperty("broker.puerto", "13000")); }

    public List<Destino> getTodosLosDestinos() { 
        return new ArrayList<>(destinos); 
    }

    public List<Destino> getDestinosActivos() {
        return destinos.stream().filter(Destino::isActivo).collect(Collectors.toList());
    }

    public static class Destino {
        private final String id;
        private final String host;
        private final int puerto;
        private final boolean activo;
        private final String tipo;

        public Destino(String id, String host, int puerto, boolean activo, String tipo) {
            this.id = id;
            this.host = host;
            this.puerto = puerto;
            this.activo = activo;
            this.tipo = tipo;
        }

        public String getId() { return id; }
        public String getHost() { return host; }
        public int getPuerto() { return puerto; }
        public boolean isActivo() { return activo; }
        public String getTipo() { return tipo; }
    }
}
