package votos;

import model.Voto;
import model.Ciudadano;

import java.util.LinkedList;
import java.util.Queue;

public class ColaMensajesConfiables {

    public static class EntradaVoto {
        public final Voto voto;
        public final Ciudadano votante;

        public EntradaVoto(Voto voto, Ciudadano votante) {
            this.voto = voto;
            this.votante = votante;
        }
    }

    private final Queue<EntradaVoto> cola = new LinkedList<>();

    public synchronized void encolar(Voto voto, Ciudadano votante) {
        cola.add(new EntradaVoto(voto, votante));
    }

    public synchronized EntradaVoto obtenerSiguiente() {
        return cola.poll();
    }

    public synchronized boolean estaVacia() {
        return cola.isEmpty();
    }
}
