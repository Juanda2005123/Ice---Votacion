package controller;

import VotingSystem.ConsultaLugarResponse;
import VotingSystem.ObserverCiudadanoPrx;
import VotingSystem.ObserverCiudadano;
import cache.CiudadanoCache;
import comunicacion.ServicioConsultaCiudadanosIce;
import dao.CiudadanoDAO;
import observer.CiudadanoObserver;
import ui.CiudadanoUI;
import java.lang.Exception;
import com.zeroc.Ice.*;

import java.sql.Connection;
import java.sql.DriverManager;

public class ControllerCiudadano {

    private CiudadanoUI ui;
    private CiudadanoCache cache;
    private CiudadanoDAO dao;
    private ServicioConsultaCiudadanosIce servicioIce;
    private Communicator communicator;

    public ControllerCiudadano() {
        this.ui = new CiudadanoUI();
        this.cache = new CiudadanoCache();
    }

    public void iniciar() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/elecciones", "postgres", "tu_clave");
            this.dao = new CiudadanoDAO(conn);

            communicator = Util.initialize();
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("CiudadanoAdapter", "tcp -p 13000");

            // Servicio de consulta
            servicioIce = new ServicioConsultaCiudadanosIce(dao, cache);
            adapter.add(servicioIce, Util.stringToIdentity("LugarVotacionService"));

            // Observador
            CiudadanoObserver observador = new CiudadanoObserver(this);
            adapter.add(observador, Util.stringToIdentity("CiudadanoObserver"));

            adapter.activate();

            // Registrar el observador en el servidor central (simulado, con proxy directo)
            ObjectPrx base = communicator.stringToProxy("ServidorCentralObserver:tcp -h localhost -p 10000");
            ObserverCiudadanoPrx proxyObservador = ObserverCiudadanoPrx.uncheckedCast(
                    adapter.createProxy(Util.stringToIdentity("CiudadanoObserver"))
            );
            // TODO: Llamar a registrarObservador(proxyObservador) en el servidor central si ya lo tienes implementado

            ui.mostrarInfo("ConsultaCiudadanos iniciado y observador registrado.");

            while (true) {
                Thread.sleep(5000);
            }

        } catch (Exception e) {
            ui.mostrarError("Error al iniciar el módulo: " + e.getMessage());
        } finally {
            if (communicator != null) communicator.destroy();
        }
    }

    // Método invocado por el Observer cuando recibe una notificación desde el servidor
    public void realizarConsultaDesdeServidor(String cedula) {
        ConsultaLugarResponse resp = servicioIce.consultarLugarVotacion(cedula, null);
        if (resp.encontrado) {
            ui.mostrarInfo("Consulta recibida del servidor para cédula: " + cedula);
            ui.mostrarInfo("→ Mesa: " + resp.mesaId + ", Lugar: " + resp.lugarNombre + ", Municipio: " + resp.ciudad);
        } else {
            ui.mostrarError("No se encontró información para cédula: " + cedula);
        }
    }
}
