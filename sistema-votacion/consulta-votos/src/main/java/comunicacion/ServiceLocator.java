package comunicacion;

import VotingSystem.QueryStationPrx;
import com.zeroc.Ice.*;

public class ServiceLocator {
    private static QueryStationPrx queryStation;

    public static void inicializar(Communicator communicator) {
        ObjectPrx base = communicator.stringToProxy("QueryStation:tcp -h 10.147.17.102 -p 12000");
        queryStation = QueryStationPrx.checkedCast(base);
        if (queryStation == null) {
            throw new RuntimeException("Proxy QueryStation no válido.");
        }
    }

    public static QueryStationPrx getQueryStation() {
        return queryStation;
    }
}