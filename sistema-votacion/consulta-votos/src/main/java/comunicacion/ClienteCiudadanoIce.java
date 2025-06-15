package comunicacion;

import VotingSystem.ObserverCiudadanoPrx;
import com.zeroc.Ice.*;
import VotingSystem.ConsultaLugarResponse;

public class ClienteCiudadanoIce {
    private ObserverCiudadanoPrx proxy;

    public ClienteCiudadanoIce(Communicator communicator) {
        ObjectPrx base = communicator.stringToProxy("ObserverCiudadano:tcp -h localhost -p 12000");
        proxy = ObserverCiudadanoPrx.checkedCast(base);
        if (proxy == null) {
            throw new RuntimeException("Proxy no inicializado correctamente.");
        }
    }

    public ConsultaLugarResponse enviarCedula(String cedula) {
        return proxy.notificarConsulta(cedula);
    }
}
