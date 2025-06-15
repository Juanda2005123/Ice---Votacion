package comunicacion;

import VotingSystem.QueryStationPrx;
import com.zeroc.Ice.*;
import java.util.HashMap;

public class ClienteCiudadanoIce {
    private QueryStationPrx proxy;
    private final HashMap<String, String> cache = new HashMap<>();

    public ClienteCiudadanoIce(Communicator communicator) {
        ObjectPrx base = communicator.stringToProxy("QueryStation:tcp -h localhost -p 12000");
        proxy = QueryStationPrx.checkedCast(base);
        if (proxy == null) {
            throw new RuntimeException("Proxy QueryStation no válido.");
        }
    }

    public String consultarLugar(String cedula) {
        if (cache.containsKey(cedula)) {
            return cache.get(cedula); 
        }

        String respuesta = proxy.query(cedula); 
        if (respuesta != null) {
            cache.put(cedula, respuesta); 
        }
        return respuesta;
    }
}
