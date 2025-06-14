package observer;

import VotingSystem.ObserverCiudadano;
import controller.ControllerCiudadano;
import com.zeroc.Ice.Current;

public class CiudadanoObserver implements ObserverCiudadano {

    private ControllerCiudadano controller;

    public CiudadanoObserver(ControllerCiudadano controller) {
        this.controller = controller;
    }

    @Override
    public void notificarConsulta(String cedula, Current current) {
        controller.realizarConsultaDesdeServidor(cedula);
    }
}
