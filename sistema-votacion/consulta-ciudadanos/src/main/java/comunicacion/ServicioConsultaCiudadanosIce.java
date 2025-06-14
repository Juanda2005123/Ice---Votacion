package comunicacion;

import VotingSystem.ConsultaLugarResponse;
import VotingSystem.LugarVotacionService;
import VotingSystem.Votante;
import dao.CiudadanoDAO;
import modelo.CiudadanoCache;
import com.zeroc.Ice.Current;

public class ServicioConsultaCiudadanosIce implements LugarVotacionService {

    private CiudadanoDAO dao;
    private CiudadanoCache cache;

    public ServicioConsultaCiudadanosIce(CiudadanoDAO dao, CiudadanoCache cache) {
        this.dao = dao;
        this.cache = cache;
    }

    @Override
    public ConsultaLugarResponse consultarLugarVotacion(String cedula, Current current) {
        ConsultaLugarResponse resp = new ConsultaLugarResponse();
        try {
            // Intentar primero por caché
            if (cache.existe(cedula)) {
                return cache.obtener(cedula);
            }

            // Consultar desde BD
            resp = dao.obtenerLugarPorCedula(cedula);

            if (resp.encontrado) {
                cache.guardar(cedula, resp);
            }

        } catch (Exception e) {
            resp.encontrado = false;
            resp.mensaje = "Error en la consulta: " + e.getMessage();
        }

        return resp;
    }
}
