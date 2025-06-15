import com.zeroc.Ice.*;
import config.ConfiguracionBroker;
import controller.BrokerConsultaController;
import comunicacion.ServidorIceBrokerConsulta;
import java.lang.Exception;

public class BrokerConsultaApp {
    public static void main(String[] args) {
        final Communicator[] communicator = new Communicator[1];

        try {
            String rutaConfig;
            if (args.length > 0) {
                rutaConfig = args[0];  // Archivo externo pasado como parametro
            } else {
                // Buscar archivo externo en directorio actual
                java.io.File archivoExterno = new java.io.File("broker-consulta.properties");
                if (archivoExterno.exists()) {
                    rutaConfig = "broker-consulta.properties";
                } else {
                    rutaConfig = "src/main/resources/broker-consulta.properties";  // Archivo interno
                }
            }
            
            ConfiguracionBroker config = new ConfiguracionBroker(rutaConfig);
            communicator[0] = Util.initialize(args);

            BrokerConsultaController controller = new BrokerConsultaController(config);
            ServidorIceBrokerConsulta servidor = new ServidorIceBrokerConsulta(controller);

            String endpoints = String.format("tcp -h %s -p %d",
                    config.getHost(), config.getPuerto());

            ObjectAdapter adapter = communicator[0].createObjectAdapterWithEndpoints("BrokerConsultaAdapter", endpoints);
            adapter.add(servidor, Util.stringToIdentity("QueryStation"));
            adapter.activate();

            System.out.println("=== BROKER DE CONSULTAS INICIADO ===");
            System.out.println("ID: " + config.getBrokerId());
            System.out.println("Nombre: " + config.getBrokerNombre());
            System.out.println("Puerto: " + config.getPuerto());

            controller.verificarConectividadInicial();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Cerrando broker de consultas...");
                controller.cerrar();
                if (communicator[0] != null) {
                    communicator[0].destroy();
                }
            }));

            communicator[0].waitForShutdown();
        } catch (Exception e) {
            System.err.println("Error iniciando el broker de consultas: " + e.getMessage());
            e.printStackTrace();
            if (communicator[0] != null) {
                communicator[0].destroy();
            }
            System.exit(1);
        }
    }
}
