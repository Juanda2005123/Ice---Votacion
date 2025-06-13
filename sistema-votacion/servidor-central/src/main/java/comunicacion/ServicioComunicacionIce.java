package comunicacion;

import VotingSystem.*;
import gestion.ProcesadorVotos;


import com.zeroc.Ice.Current;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * Esta clase implementa el patrón Broker
 * Actua como intermediario entre el cliente remoto y la lógica de negocio del servidor.
 * 
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
    public boolean enviarVotoVotante(String mesaId, Voto voto, Ciudadano votante, Current current) {
        //  Validación del broker antes de delegar (patrón Broker)
        if (mesaId == null || mesaId.trim().isEmpty()) {
            System.err.println("Broker: Mesa inválida");
            return false;
        }

        try {
            
            Future<Boolean> resultado = threadPool.submit(() -> {
               
                model.Voto votoJava = convertirVotoIceAJava(voto);
                model.Ciudadano votanteJava = convertirVotanteIceAJava(votante);

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
        model.Voto votoJava = new model.Voto(
                            votoIce.votoId,
                            candidatoJava,
                            java.time.LocalDateTime.parse(votoIce.timestamp)
                            , "MESA_DEFAULT" // Default mesaId, can be changed later
                        );
                                      
        
        return votoJava;
    }

    private model.Ciudadano convertirVotanteIceAJava(Ciudadano votanteIce) {
        model.Ciudadano votanteJava = new model.Ciudadano(
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