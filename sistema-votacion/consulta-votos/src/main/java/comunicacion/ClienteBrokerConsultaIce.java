package comunicacion;

import VotingSystem.QueryStationPrx;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;
import com.zeroc.Ice.Communicator;

public class ClienteBrokerConsultaIce {
    private final QueryStationPrx proxy;

    public ClienteBrokerConsultaIce(Communicator communicator) {
        ObjectPrx base = communicator.stringToProxy( "QueryStation:tcp -h 10.147.17.102 -p 12000");
        proxy = QueryStationPrx.checkedCast(base);
        if (proxy == null) {
            throw new RuntimeException("No se pudo obtener el proxy del Broker de Consultas");
        }
    }

    public String consultarLugar(String cedula) {
        return proxy.query(cedula);
    }
}
