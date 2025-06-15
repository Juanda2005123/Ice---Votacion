package comunicacion;

import VotingSystem.QueryStationPrx;
import com.zeroc.Ice.*;

public class ClienteCiudadanoIce {
    private QueryStationPrx proxy;

    public ClienteCiudadanoIce(Communicator communicator) {
        ObjectPrx base = communicator.stringToProxy("QueryStation:tcp -h localhost -p 12000");
        proxy = QueryStationPrx.checkedCast(base);
        if (proxy == null) {
            throw new RuntimeException("Proxy QueryStation no válido.");
        }
    }

    public String consultarLugar(String cedula) {
        return proxy.query(cedula);
    }
}
