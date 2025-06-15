package comunicacion;

import VotingSystem.BrokerServicePrx;
import com.zeroc.Ice.*;

public class ClienteBrokerConsultaIce {
    private final BrokerServicePrx proxy;

    public ClienteBrokerConsultaIce(Communicator communicator) {
        ObjectPrx base = communicator.stringToProxy("BrokerService:tcp -h localhost -p 12000");
        proxy = BrokerServicePrx.checkedCast(base);
        if (proxy == null) {
            throw new RuntimeException("No se pudo obtener el proxy del Broker");
        }
    }

    public String consultarLugar(String cedula) {
        return proxy.query(cedula); // Este método debe estar implementado en el broker
    }
}
