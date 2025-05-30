package servidor;

import modelo.RegistroVotos;
import modelo.Voto;

public class ServicioStream {
    private RegistroVotos registro;

    public ServicioStream(RegistroVotos registro) {
        this.registro = registro;
    }

    public boolean recibirVoto(Voto voto) {
        if (registro.esVotoDuplicado(voto.getId())) {
            System.out.println("Voto duplicado rechazado: " + voto.getId());
            return false;
        } else {
            registro.registrarVoto(voto.getId());
            System.out.println("Voto recibido: " + voto.getId() + " - Candidato: " + voto.getCandidato());
            return true;
        }
    }
}
