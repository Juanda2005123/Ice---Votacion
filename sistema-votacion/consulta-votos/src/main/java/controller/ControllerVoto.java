package controller;

import VotingSystem.ConsultaLugarResponse;
import comunicacion.ClienteCiudadanoIce;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Util;
import ui.VotoUI;

public class ControllerVoto {

    private Communicator communicator;
    private ClienteCiudadanoIce cliente;
    private VotoUI ui;

    public ControllerVoto() {
        this.ui = new VotoUI();
    }

    public void iniciar() {
        try {
            communicator = Util.initialize();
            cliente = new ClienteCiudadanoIce(communicator);
            ui.mostrarInfo("ConsultaVotos iniciado correctamente.");
        } catch (Exception e) {
            ui.mostrarError("Error al iniciar: " + e.getMessage());
        }
    }

    public void consultarLugarVotacion(String cedula) {
        try {
            ConsultaLugarResponse resp = cliente.consultarLugar(cedula);
            if (resp.encontrado) {
                ui.mostrarInfo("Mesa: " + resp.mesaId);
                ui.mostrarInfo("Puesto: " + resp.lugarNombre);
                ui.mostrarInfo("Municipio: " + resp.ciudad);
                ui.mostrarInfo("Departamento: " + resp.departamento);
                ui.mostrarInfo("Dirección: " + resp.direccion);
            } else {
                ui.mostrarError("No se encontró información para la cédula: " + cedula);
            }
        } catch (Exception e) {
            ui.mostrarError("Error en la consulta: " + e.getMessage());
        }
    }

    public void cerrar() {
        if (communicator != null) {
            communicator.destroy();
        }
    }
}
