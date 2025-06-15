package comunicacion;

import VotingSystem.QueryStationPrx;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;
import config.ConfiguracionBroker;
import java.lang.Exception;

public class ServicioVerificacionConectividad {

    private Communicator communicator;

    public ServicioVerificacionConectividad() {
        try {
            this.communicator = Util.initialize();
        } catch (Exception e) {
            throw new RuntimeException("Error inicializando communicator para verificacion: " + e.getMessage());
        }
    }

    public boolean verificarConectividad(ConfiguracionBroker.Destino destino) {
        try {
            String proxyString = String.format("QueryStation:tcp -h %s -p %d", destino.getHost(), destino.getPuerto());
            ObjectPrx base = communicator.stringToProxy(proxyString);
            QueryStationPrx proxy = QueryStationPrx.checkedCast(base);
            return proxy != null && proxy.ping();
        } catch (Exception e) {
            return false;
        }
    }

    public void verificarConectividadDestinos(ConfiguracionBroker config) {
        for (ConfiguracionBroker.Destino destino : config.getTodosLosDestinos()) {
            boolean ok = verificarConectividad(destino);
            System.out.printf("[%-3s] %s en %s:%d\n",
                    ok ? "OK" : "X", destino.getId(), destino.getHost(), destino.getPuerto());
        }
    }

    public void cerrar() {
        if (communicator != null) {
            communicator.destroy();
        }
    }

    public static class InfoNodo {
        private String id;
        private String nombre;
        private String tipo;

        public InfoNodo(String id, String nombre, String tipo) {
            this.id = id;
            this.nombre = nombre;
            this.tipo = tipo;
        }

        public String getId() { return id; }
        public String getNombre() { return nombre; }
        public String getTipo() { return tipo; }
    }
}