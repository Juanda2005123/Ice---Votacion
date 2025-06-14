package comunicacion;

import VotingSystem.ConsultaLugarResponse;
import VotingSystem.LugarVotacionService;
import cache.CiudadanoCache;
import dao.CiudadanoDAO;

import com.zeroc.Ice.Current;

import java.sql.SQLException;
import java.util.Map;

public class ServicioConsultaCiudadanosIce implements LugarVotacionService {

    private CiudadanoDAO dao;
    private CiudadanoCache cache;

    public ServicioConsultaCiudadanosIce(CiudadanoDAO dao, CiudadanoCache cache) {
        this.dao = dao;
        this.cache = cache;
    }

    @Override
    public ConsultaLugarResponse consultarLugarVotacion(String cedula, Current current) {
        ConsultaLugarResponse response = new ConsultaLugarResponse();

        try {
            Map<String, String> datos;

            if (cache.contiene(cedula)) {
                datos = cache.get(cedula);
                response.mensaje = "Resultado obtenido de caché.";
            } else {
                datos = dao.consultarLugarPorCedula(cedula);
                if (datos != null) cache.put(cedula, datos);
                response.mensaje = "Resultado obtenido de base de datos.";
            }

            if (datos != null) {
                response.departamento = datos.get("departamento");
                response.ciudad = datos.get("municipio");
                response.lugarNombre = datos.get("puesto");
                response.direccion = datos.get("direccion");
                response.mesaId = datos.get("mesa");
                response.encontrado = true;
            } else {
                response.encontrado = false;
                response.mensaje = "Ciudadano no encontrado.";
            }

        } catch (SQLException e) {
            response.encontrado = false;
            response.mensaje = "Error al consultar: " + e.getMessage();
        }

        return response;
    }
}
