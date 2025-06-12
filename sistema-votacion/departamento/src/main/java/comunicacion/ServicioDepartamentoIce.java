package comunicacion;

import VotingSystem.DepartamentoService;
import VotingSystem.LugarVotacionServicePrx;
import com.zeroc.Ice.Current;

import gestion.RegistroLugares;

import java.util.List;

public class ServicioDepartamentoIce implements DepartamentoService {

    private RegistroLugares registro;

    public ServicioDepartamentoIce(RegistroLugares registro) {
        this.registro = registro;
    }

    @Override
    public void registrarLugarVotacion(String lugarId, LugarVotacionServicePrx proxy, Current current) {
        registro.registrar(lugarId, proxy);
    }

    @Override
    public String[] obtenerLugaresRegistrados(Current current) {
        List<String> lista = registro.getLugaresRegistrados();
        return lista.toArray(new String[0]);
    }


    @Override
    public String asignarLugarParaConsulta(String cedula, Current current) {
        return registro.asignarPorHash(cedula);
    }
}

