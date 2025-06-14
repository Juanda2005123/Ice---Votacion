package cache;

import java.util.HashMap;
import java.util.Map;

public class CiudadanoCache {
    private final Map<String, Map<String, String>> cache = new HashMap<>();

    public Map<String, String> get(String cedula) {
        return cache.get(cedula);
    }

    public void put(String cedula, Map<String, String> datos) {
        cache.put(cedula, datos);
    }

    public boolean contiene(String cedula) {
        return cache.containsKey(cedula);
    }
}
