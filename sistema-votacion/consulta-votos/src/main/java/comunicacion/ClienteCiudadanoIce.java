package comunicacion;

import java.util.HashMap;
import java.util.Map;

public class ClienteCiudadanoIce {
    private final Map<String, String> cache = new HashMap<>();

    public String consultarLugar(String cedula) {
        return cache.computeIfAbsent(cedula, c -> ServiceLocator.getQueryStation().query(c));
    }
}