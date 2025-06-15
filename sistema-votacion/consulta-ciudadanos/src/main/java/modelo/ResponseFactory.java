package modelo;

import VotingSystem.ConsultaLugarResponse;

public class ResponseFactory {
    public static ConsultaLugarResponse crearError(String mensaje) {
        ConsultaLugarResponse r = new ConsultaLugarResponse();
        r.encontrado = false;
        r.mensaje = mensaje;
        return r;
    }

    public static ConsultaLugarResponse crearExito(String nombre, String apellido, String depto, String ciudad, String lugar, String direccion, String mesa) {
        ConsultaLugarResponse r = new ConsultaLugarResponse();
        r.encontrado = true;
        r.mensaje = nombre + " " + apellido;
        r.departamento = depto;
        r.ciudad = ciudad;
        r.lugarNombre = lugar;
        r.direccion = direccion;
        r.mesaId = mesa;
        return r;
    }
}