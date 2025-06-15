package comunicacion;

import VotingSystem.QueryStation;
import com.zeroc.Ice.Current;
import VotingSystem.ConsultaLugarResponse;
import dao.CiudadanoDAO;

public class QueryStationImpl implements QueryStation {
    private CiudadanoDAO dao;

    public QueryStationImpl(CiudadanoDAO dao) {
        this.dao = dao;
    }

    @Override
    public String query(String document, Current current) {
        ConsultaLugarResponse response = dao.consultarLugarPorCedula(document);
        if (!response.encontrado) return null;

        return String.format("Usted debe votar en %s ubicado en %s en %s, %s en la mesa %s.",
                response.lugarNombre,
                response.direccion,
                response.ciudad,
                response.departamento,
                response.mesaId
        );
    }

    @Override
    public boolean ping(Current current) {
        return true;
    }
}
