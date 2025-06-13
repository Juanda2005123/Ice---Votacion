package controller;

import comunicacion.ServicioLugarVotacionIce;
import ui.LugarUI;
import com.zeroc.Ice.*;

import VotingSystem.DepartamentoServicePrx;
import VotingSystem.LugarVotacionServicePrx;
import VotingSystem.LugarVotacionService;

public class ControllerLugar {

    private LugarUI ui;
    private ServicioLugarVotacionIce servicio;
    private Communicator communicator;

    public ControllerLugar() {
        this.ui = new LugarUI();
    }

    public void iniciar() {
        try {
            communicator = Util.initialize();
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("LugarAdapter", "tcp -p 12000");

            servicio = new ServicioLugarVotacionIce("Lugar-01"); // nombre identificador
            adapter.add(servicio, Util.stringToIdentity("LugarVotacionService"));
            adapter.activate();

            // Conectarse al departamento para registrarse
            ObjectPrx departamentoBase = communicator.stringToProxy("DepartamentoService:tcp -h localhost -p 11000");
            DepartamentoServicePrx departamento = DepartamentoServicePrx.checkedCast(departamentoBase);
            LugarVotacionServicePrx proxyLugar = LugarVotacionServicePrx.uncheckedCast(
                    adapter.createProxy(Util.stringToIdentity("LugarVotacionService"))
            );

            departamento.registrarLugarVotacion("Lugar-01", proxyLugar);

            ui.mostrarMensajeInfo("Lugar de votación registrado correctamente en el departamento.");

            while (true) {
                Thread.sleep(5000); // mantener activo
            }

        } catch (java.lang.Exception e) {
            ui.mostrarMensajeError("Error en lugar de votación: " + e.getMessage());
        } finally {
            if (communicator != null) {
                communicator.destroy();
            }
        }
    }
}
