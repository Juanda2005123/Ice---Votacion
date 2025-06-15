package controller;

import comunicacion.ClienteCiudadanoIce;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Util;
import ui.VotoUI;

public class ControllerVoto {
    private VotoUI ui;
    private ClienteCiudadanoIce cliente;
    private Communicator communicator;

    public ControllerVoto() {
        this.ui = new VotoUI();
    }

    public void iniciar() {
        try {
            communicator = Util.initialize();
            cliente = new ClienteCiudadanoIce(communicator);
            ui.mostrarInfo("ConsultaVotos iniciado correctamente.");
        } catch (Exception e) {
            ui.mostrarError("Falló la conexión con QueryStation: " + e.getMessage());
        }
    }

    public void consultarLugarPorCedula(String cedula) {
        try {
            String respuesta = cliente.consultarLugar(cedula);
            if (respuesta == null) {
                ui.mostrarError("No se encontró información para la cédula: " + cedula);
            } else {
                ui.mostrarInfo(respuesta);
            }
        } catch (Exception e) {
            ui.mostrarError("Error al enviar la cédula: " + e.getMessage());
        }
    }

    public void cerrar() {
        if (communicator != null) communicator.destroy();
    }
}
