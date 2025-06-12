package controller;

import comunicacion.ServicioDepartamentoIce;
import ui.DepartamentoUI;
import gestion.RegistroLugares;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;

public class ControllerDepartamento {

    private DepartamentoUI ui;
    private ServicioDepartamentoIce servicioIce;
    private Communicator communicator;

    public ControllerDepartamento() {
        this.ui = new DepartamentoUI();
    }

    public void iniciar() {
        try {
            communicator = com.zeroc.Ice.Util.initialize();
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("DepartamentoAdapter", "tcp -p 11000");

            servicioIce = new ServicioDepartamentoIce(new RegistroLugares(this));
            adapter.add(servicioIce, com.zeroc.Ice.Util.stringToIdentity("DepartamentoService"));
            adapter.activate();

            ui.mostrarMensajeInfo("Departamento iniciado en puerto 11000");

            while (true) {
                Thread.sleep(5000); // mantener vivo
            }

        } catch (Exception e) {
            ui.mostrarMensajeError("Error al iniciar el Departamento: " + e.getMessage());
        } finally {
            if (communicator != null) {
                communicator.destroy();
            }
        }
    }

    public DepartamentoUI getUI() {
        return ui;
    }
}
