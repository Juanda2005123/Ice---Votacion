package enrutamiento;

import config.ConfiguracionBroker;
import java.util.List;
import java.util.Random;

public class EstrategiaEnrutamientoConsulta {

    private final ConfiguracionBroker config;
    private final Random random;

    public EstrategiaEnrutamientoConsulta(ConfiguracionBroker config) {
        this.config = config;
        this.random = new Random();
    }

    public ConfiguracionBroker.Destino seleccionarDestino() {
        List<ConfiguracionBroker.Destino> activos = config.getDestinosActivos();
        if (activos.isEmpty()) return null;
        return activos.get(random.nextInt(activos.size()));
    }
}