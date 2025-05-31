package comunicacion;

import VotingSystem.*;
import gestion.ProcesadorVotos;

import com.zeroc.Ice.Current;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * Servant Ice que implementa el servicio de votación.
 * Recibe votos desde las mesas y los procesa.
 */
public class ServicioComunicacionIce implements VotingService {
    
    private ProcesadorVotos procesadorVotos;
    private ExecutorService threadPool;


    public ServicioComunicacionIce(ProcesadorVotos procesadorVotos) {
        this.procesadorVotos = procesadorVotos;
        this.threadPool = Executors.newFixedThreadPool(10); 
    }
    
    @Override
    public boolean enviarVotoVotante(String mesaId, Voto voto, Votante votante, Current current) {
        try {
            
            Future<Boolean> resultado = threadPool.submit(() -> {
               
                model.Voto votoJava = convertirVotoIceAJava(voto);
                model.Votante votanteJava = convertirVotanteIceAJava(votante);

                boolean votoOK = procesadorVotos.procesarVoto(votoJava, mesaId);
                boolean votanteOK = procesadorVotos.procesarVotante(votanteJava, mesaId);
                return votoOK && votanteOK;
            });

            
            return resultado.get();

        } catch (Exception e) {
            System.err.println("Error procesando voto con thread pool: " + e.getMessage());
            return false;
        }
    }

    
    // Métodos de conversión Ice ↔ Java
    private model.Voto convertirVotoIceAJava(Voto votoIce) {
        // Convertir Candidato (Ice classes have public fields)
        model.Candidato candidatoJava = new model.Candidato(
            votoIce.candidato.cedula,
            votoIce.candidato.cedula,
            votoIce.candidato.nombre,
            votoIce.candidato.apellido,
            votoIce.candidato.partidoPolitico
        );
        
        // Crear Voto Java (need to provide all required parameters)
        model.Voto votoJava = new model.Voto(candidatoJava, 
                                           java.time.LocalDateTime.now(),                                           "MESA_DEFAULT");
        
        return votoJava;
    }

    private model.Votante convertirVotanteIceAJava(Votante votanteIce) {
        model.Votante votanteJava = new model.Votante(
            votanteIce.cedula,
            votanteIce.nombre,
            votanteIce.apellido,
            votanteIce.departamento,
            votanteIce.ciudad,
            "MESA_DEFAULT",
            "LUGAR_DEFAULT"
        );
        
        votanteJava.setYaVoto(votanteIce.yaVoto);
        
        return votanteJava;
    }

    @Override
    public int getTotalVotos(Current current) {
        // Return total votes processed
        return 0; // TODO: implement proper counting
    }

    public void shutdownThreadPool() {
        if (threadPool != null) {
            threadPool.shutdown();
        }
    }

}