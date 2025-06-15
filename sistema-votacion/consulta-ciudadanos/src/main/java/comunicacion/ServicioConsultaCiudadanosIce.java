package comunicacion;

import VotingSystem.ObserverCiudadano;
import VotingSystem.ConsultaLugarResponse;
import dao.CiudadanoDAO;
import ui.CiudadanoUI;

public class ServicioConsultaCiudadanosIce implements ObserverCiudadano {
    private CiudadanoDAO dao;
    private CiudadanoUI ui;

    public ServicioConsultaCiudadanosIce(CiudadanoDAO dao, CiudadanoUI ui) {
        this.dao = dao;
        this.ui = ui;
    }

@Override
public ConsultaLugarResponse notificarConsulta(String cedula, com.zeroc.Ice.Current current) {
    ConsultaLugarResponse response = dao.consultarLugarPorCedula(cedula);
    if (response.encontrado) {
        ui.mostrarInfo("Información entregada para la cédula: " + cedula);
    } else {
        ui.mostrarError("No se encontró información para la cédula: " + cedula);
    }
    return response;
}
}
