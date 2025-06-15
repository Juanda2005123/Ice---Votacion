package controller;

import com.zeroc.Ice.*;
import comunicacion.ServicioConsultaCiudadanosIce;
import dao.CiudadanoDAO;
import ui.CiudadanoUI;
import java.lang.Exception;

public class ControllerCiudadano {
    private CiudadanoUI ui = new CiudadanoUI();

    public void iniciar() {
        try (Communicator communicator = Util.initialize()) {
            CiudadanoDAO dao = new CiudadanoDAO();
            ServicioConsultaCiudadanosIce observador = new ServicioConsultaCiudadanosIce(dao, ui);

            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints(
                    "ObserverCiudadanoAdapter", "default -p 12000");
            adapter.add(observador, Util.stringToIdentity("ObserverCiudadano"));
            adapter.activate();

            ui.mostrarInfo("ConsultaCiudadanos iniciado y observador registrado.");
            communicator.waitForShutdown();
        } catch (Exception e) {
            ui.mostrarError("Error al iniciar el servidor ICE: " + e.getMessage());
        }
    }
}
