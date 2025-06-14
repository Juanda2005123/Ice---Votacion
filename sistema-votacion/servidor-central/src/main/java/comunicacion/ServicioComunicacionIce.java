package comunicacion;

import VotingSystem.*;
import gestion.ProcesadorVotos;
import com.zeroc.Ice.Current;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Servant Ice que implementa el servicio de votación.
 * NOTA: Implementación temporalmente deshabilitada.
 * Los métodos lanzan UnsupportedOperationException hasta que se implemente completamente.
 * 
 * Esta clase implementa el patrón Broker
 * Actua como intermediario entre el cliente remoto y la lógica de negocio del servidor.
 */
public class ServicioComunicacionIce implements VotingSystem.ReceptorVotos {
    
    private ProcesadorVotos procesadorVotos;
    private ExecutorService threadPool;

    public ServicioComunicacionIce(ProcesadorVotos procesadorVotos) {
        this.procesadorVotos = procesadorVotos;
        this.threadPool = Executors.newFixedThreadPool(10);
        System.out.println("ServicioComunicacionIce (servidor-central): Implementación temporalmente deshabilitada");
    }
    
    @Override
    public boolean recibirVoto(Voto voto, Current current) {
        throw new UnsupportedOperationException(
            "ServicioComunicacionIce.recibirVoto() no está implementado aún. " +
            "Voto ID: " + (voto != null ? voto.id : "null")
        );
    }
    
    @Override
    public boolean ping(Current current) {
        throw new UnsupportedOperationException(
            "ServicioComunicacionIce.ping() no está implementado aún."
        );
    }
    
    /**
     * Método para compatibilidad con versiones anteriores.
     * NOTA: Temporalmente deshabilitado.
     * 
     * @throws UnsupportedOperationException Método no implementado aún
     */
    public boolean enviarVotoVotante(String mesaId, Voto voto, Ciudadano votante, Current current) {
        throw new UnsupportedOperationException(
            "ServicioComunicacionIce.enviarVotoVotante() no está implementado aún. " +
            "Mesa: " + mesaId + ", Voto: " + (voto != null ? voto.id : "null") + 
            ", Votante: " + (votante != null ? votante.documento : "null")
        );
    }
    
    /**
     * Métodos de conversión Ice ↔ Java.
     * NOTA: Temporalmente deshabilitados.
     */
    private model.Voto convertirVotoIceAJava(Voto votoIce) {
        throw new UnsupportedOperationException(
            "ServicioComunicacionIce.convertirVotoIceAJava() no está implementado aún."
        );
    }

    private model.Ciudadano convertirVotanteIceAJava(Ciudadano votanteIce) {
        throw new UnsupportedOperationException(
            "ServicioComunicacionIce.convertirVotanteIceAJava() no está implementado aún."
        );
    }
    
    /**
     * Cierra el thread pool de manera controlada.
     * NOTA: Este método sí está implementado para evitar problemas de recursos.
     */
    public void shutdownThreadPool() {
        if (threadPool != null) {
            threadPool.shutdown();
            System.out.println("Thread pool del ServicioComunicacionIce cerrado");
        }
    }
    
    /**
     * Obtiene el procesador de votos asociado.
     * NOTA: Temporalmente deshabilitado.
     * 
     * @throws UnsupportedOperationException Método no implementado aún
     */
    public ProcesadorVotos getProcesadorVotos() {
        throw new UnsupportedOperationException(
            "ServicioComunicacionIce.getProcesadorVotos() no está implementado aún."
        );
    }
    
    /**
     * Verifica el estado del servicio.
     * NOTA: Temporalmente deshabilitado.
     * 
     * @throws UnsupportedOperationException Método no implementado aún
     */
    public boolean verificarEstado() {
        throw new UnsupportedOperationException(
            "ServicioComunicacionIce.verificarEstado() no está implementado aún."
        );
    }
}