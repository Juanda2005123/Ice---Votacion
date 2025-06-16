import com.zeroc.Ice.*;
import dao.CiudadanoDAO;
import comunicacion.QueryStationImpl;
import VotingSystem.QueryStation;
import java.lang.Exception;

public class ConsultaCiudadanoApp {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {
            // Leer parámetros de sistema
            String instanceId = System.getProperty("instance.id", "consulta-A");
            int port = Integer.parseInt(System.getProperty("instance.port", "12001"));

            CiudadanoDAO dao = new CiudadanoDAO();
            QueryStationImpl queryStation = new QueryStationImpl(dao);

            String endpoints = String.format("tcp -h localhost -p %d", port);
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("Adapter" + instanceId, endpoints);
            adapter.add(queryStation, Util.stringToIdentity("QueryStation"));
            adapter.activate();

            System.out.println("[Ok] Instancia " + instanceId + " iniciada en puerto " + port);
            communicator.waitForShutdown();

        } catch (Exception e) {
            System.err.println("[ERROR] Error al iniciar el servidor ICE: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
