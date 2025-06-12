package gestion;

import VotingSystem.LugarVotacionServicePrx;
import controller.ControllerDepartamento;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RegistroLugares {

    private ControllerDepartamento controller;
    private Map<String, LugarVotacionServicePrx> lugares;

    public RegistroLugares(ControllerDepartamento controller) {
        this.controller = controller;
        this.lugares = new ConcurrentHashMap<>();
    }

    public void registrar(String id, LugarVotacionServicePrx proxy) {
        lugares.put(id, proxy);
        controller.getUI().mostrarMensajeInfo("Lugar registrado: " + id);
    }

    public List<String> getLugaresRegistrados() {
        return new ArrayList<>(lugares.keySet());
    }

    public String asignarPorHash(String cedula) {
        if (lugares.isEmpty()) return "";
        List<String> llaves = new ArrayList<>(lugares.keySet());
        int idx = Math.abs(cedula.hashCode()) % llaves.size();
        return llaves.get(idx);
    }

    public LugarVotacionServicePrx getLugar(String lugarId) {
        return lugares.get(lugarId);
    }
}
