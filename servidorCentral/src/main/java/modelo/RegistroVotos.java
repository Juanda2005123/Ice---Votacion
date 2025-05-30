package modelo;

import java.util.HashSet;
import java.util.Set;

public class RegistroVotos {
    private Set<String> idsVotosRecibidos = new HashSet<>();

    public boolean esVotoDuplicado(String idVoto) {
        return idsVotosRecibidos.contains(idVoto);
    }

    public void registrarVoto(String idVoto) {
        idsVotosRecibidos.add(idVoto);
    }
}
