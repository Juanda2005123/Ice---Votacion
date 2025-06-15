package comunicacion;

import VotingSystem.QueryStationPrx;
import com.zeroc.Ice.*;
import config.ConfiguracionBroker;
import java.lang.Exception;

public class ServicioComunicacionConsultaBroker {

    private final Communicator communicator;
    private final ConfiguracionBroker config;

    public ServicioComunicacionConsultaBroker(ConfiguracionBroker config) {
        this.config = config;
        this.communicator = Util.initialize();
    }

    public String reenviarConsultaLugar(String cedula) {
        ConfiguracionBroker.Destino destino = seleccionarDestino();
        if (destino == null) return "ERROR: No hay destinos disponibles.";

        try {
            String proxyStr = String.format("QueryStation:tcp -h %s -p %d",
                    destino.getHost(), destino.getPuerto());
            ObjectPrx base = communicator.stringToProxy(proxyStr);
            QueryStationPrx prx = QueryStationPrx.checkedCast(base);

            if (prx == null || !prx.ping()) return "ERROR: Fallo de conexion.";
            return prx.query(cedula);

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    private ConfiguracionBroker.Destino seleccionarDestino() {
        return config.getDestinosActivos().isEmpty() ? null : config.getDestinosActivos().get(0);
    }

    public void cerrarConexion() {
        if (communicator != null) {
            communicator.destroy();
        }
    }
}
