package controller;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;
import comunicacion.ServicioConsultaCiudadanosIce;
import dao.CiudadanoDAO;
import modelo.CiudadanoCache;
import ui.CiudadanoUI;

public class ControllerCiudadano {

    private CiudadanoUI ui;
    private CiudadanoCache cache;
    private Communicator communicator;

    public ControllerCiudadano() {
        this.ui = new CiudadanoUI();
        this.cache = new CiudadanoCache();
    }

    public void iniciar() {
        try {
            CiudadanoDAO dao = new CiudadanoDAO();
            communicator = Util.initialize();
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("CiudadanoAdapter", "tcp -p 10001");
            ServicioConsultaCiudadanosIce servicio = new ServicioConsultaCiudadanosIce(dao, cache);

            adapter.add(servicio, Util.stringToIdentity("LugarVotacionService"));
            adapter.activate();

            ui.mostrarInfo("ConsultaCiudadanos iniciado y listo para responder.");
            while (true) Thread.sleep(10000);

        } catch (Exception e) {
            ui.mostrarError("Error al iniciar: " + e.getMessage());
        } finally {
            if (communicator != null) communicator.destroy();
        }
    }
}
