package modelo;

import java.util.HashMap;
import java.util.Map;

public class Resultado {
    private Map<String, Integer> votosPorCandidato = new HashMap<>();

    public void registrarVoto(String candidato) {
        votosPorCandidato.put(candidato, votosPorCandidato.getOrDefault(candidato, 0) + 1);
    }

    public int obtenerVotos(String candidato) {
        return votosPorCandidato.getOrDefault(candidato, 0);
    }

    public Map<String, Integer> getTodosLosResultados() {
        return votosPorCandidato;
    }
}
