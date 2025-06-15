package controller;

import comunicacion.ClienteBrokerConsultaIce;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Util;
import ui.VotoUI;

public class ControllerVoto {

    private VotoUI ui;
    private ClienteBrokerConsultaIce cliente;
    private Communicator communicator;

    public ControllerVoto() {
        this.ui = new VotoUI();
    }

    public void iniciar() {
        try {
            communicator = Util.initialize();
            cliente = new ClienteBrokerConsultaIce(communicator); // Conecta con el broker
            ui.mostrarInfo("ConsultaVotos conectado al broker correctamente.");
        } catch (Exception e) {
            ui.mostrarError("Falló la conexión con el Broker: " + e.getMessage());
        }
    }

    public void consultarLugarPorCedula(String cedula) {
        try {
            String respuesta = cliente.consultarLugar(cedula);
            if (respuesta == null || respuesta.isEmpty()) {
                ui.mostrarError("No se encontró información para la cédula: " + cedula);
            } else {
                ui.mostrarInfo(respuesta);
            }
        } catch (Exception e) {
            ui.mostrarError("Error al consultar la cédula: " + e.getMessage());
        }
    }

    public void cerrar() {
        if (communicator != null) {
            communicator.destroy();
        }
    }
}
