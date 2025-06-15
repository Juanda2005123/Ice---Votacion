package comunicacion;

import VotingSystem.QueryStation;
import com.zeroc.Ice.Current;
import controller.BrokerConsultaController;

public class ServidorIceBrokerConsulta implements QueryStation {

    private final BrokerConsultaController controller;

    public ServidorIceBrokerConsulta(BrokerConsultaController controller) {
        this.controller = controller;
    }

    @Override
    public String query(String document, Current current) {
        return controller.consultarLugar(document);
    }

    @Override
    public boolean ping(Current current) {
        return true;
    }
}
