package controller;

import com.zeroc.Ice.*;
import comunicacion.QueryStationImpl;
import dao.CiudadanoDAO;
import ui.CiudadanoUI;
import java.lang.Exception;

public class ControllerCiudadano {
    private CiudadanoUI ui = new CiudadanoUI();

    public void iniciar() {
        try (Communicator communicator = Util.initialize()) {
            CiudadanoDAO dao = CiudadanoDAO.getInstance();
            QueryStationImpl servicio = new QueryStationImpl(dao);

            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints(
                    "QueryStationAdapter", "default -p 12000");
            adapter.add(servicio, Util.stringToIdentity("QueryStation"));
            adapter.activate();

            ui.mostrarInfo("QueryStation registrado y listo para consultas.");
            communicator.waitForShutdown();
        } catch (Exception e) {
            ui.mostrarError("Error al iniciar el servidor ICE: " + e.getMessage());
        }
    }
}
