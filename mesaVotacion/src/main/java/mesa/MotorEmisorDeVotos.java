package mesa;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Current;

import modelo.Voto;
import servidor.ServicioStream;

import java.util.UUID;

public class MotorEmisorDeVotos {
    private ServicioStream servidor;

    public MotorEmisorDeVotos(ServicioStream servidor) {
        this.servidor = servidor;
    }

    public void emitirVoto(String cedula, String candidato, String mesaId) {
        String votoId = UUID.randomUUID().toString();  // Genera un ID único para el voto
        Voto voto = new Voto(votoId, cedula, candidato, mesaId);

        boolean confirmado = servidor.recibirVoto(voto);

        if (confirmado) {
            System.out.println("Voto emitido correctamente.");
        } else {
            System.out.println("No se pudo emitir el voto (duplicado o error).");
        }
    }
}
