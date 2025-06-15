package controller;

import comunicacion.ClienteCiudadanoIce;
import com.zeroc.Ice.Communicator;
import VotingSystem.ConsultaLugarResponse;
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
            ui.mostrarError("Falló la conexión con ObserverCiudadano: " + e.getMessage());
        }
    }

    public void consultarLugarPorCedula(String cedula) {
        try {
            ConsultaLugarResponse resp = cliente.enviarCedula(cedula);
            if (resp.encontrado) {
                ui.mostrarInfo("→ Nombre: " + resp.mensaje);
                ui.mostrarInfo("→ Mesa: " + resp.mesaId);
                ui.mostrarInfo("→ Lugar: " + resp.lugarNombre);
                ui.mostrarInfo("→ Municipio: " + resp.ciudad);
                ui.mostrarInfo("→ Departamento: " + resp.departamento);
            } else {
                ui.mostrarError("No se encontró información para la cédula: " + cedula);
            }
        } catch (Exception e) {
            ui.mostrarError("Error al enviar la cédula: " + e.getMessage());
        }
    }


    public void cerrar() {
        if (communicator != null) communicator.destroy();
    }
}
