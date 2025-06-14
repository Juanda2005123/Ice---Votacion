package modelo;

import VotingSystem.ConsultaLugarResponse;
import java.util.concurrent.ConcurrentHashMap;

public class CiudadanoCache {
    private final ConcurrentHashMap<String, ConsultaLugarResponse> cache = new ConcurrentHashMap<>();

    public boolean existe(String cedula) {
        return cache.containsKey(cedula);
    }

    public void guardar(String cedula, ConsultaLugarResponse resp) {
        cache.put(cedula, resp);
    }

    public ConsultaLugarResponse obtener(String cedula) {
        return cache.get(cedula);
    }
}
