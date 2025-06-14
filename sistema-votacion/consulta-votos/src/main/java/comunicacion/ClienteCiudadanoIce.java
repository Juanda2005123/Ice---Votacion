package comunicacion;

import VotingSystem.ConsultaLugarResponse;
import VotingSystem.LugarVotacionServicePrx;
import com.zeroc.Ice.*;

public class ClienteCiudadanoIce {
    private LugarVotacionServicePrx proxy;

    public ClienteCiudadanoIce(Communicator communicator) {
        ObjectPrx base = communicator.stringToProxy("LugarVotacionService:tcp -h localhost -p 12000");
        proxy = LugarVotacionServicePrx.checkedCast(base);
    }

    public ConsultaLugarResponse consultarLugar(String cedula) {
        return proxy.consultarLugarVotacion(cedula);
    }
}
