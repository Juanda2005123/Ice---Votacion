package votos;

import model.Voto;
import model.Ciudadano;
import comunicacion.ServicioComunicacionIce;

/**
 * Coordinador de envio de votos que maneja la logica de envio al broker.
 * Coordina entre el repositorio local y el servicio de comunicacion Ice.
 * 
 * @author Sistema de Votacion
 * @version 2.0 - Simplificado para envio directo de votos
 * @since 2025-06-14
 */
public class CoordinadorEnvioVotos {
    
    private RepositorioMesaVotacion repositorio;
    private ServicioComunicacionIce servicioIce;
    
    /**
     * Constructor del Coordinador de Envio de Votos.
     * 
     * @param repositorio Repositorio de mesa de votacion
     */
    public CoordinadorEnvioVotos(RepositorioMesaVotacion repositorio, ServicioComunicacionIce servicioIce) {
        if (repositorio == null) {
            throw new IllegalArgumentException("El repositorio no puede ser null");
        }
        
        this.repositorio = repositorio;
        this.servicioIce = servicioIce;
    }
    
    /**
     * Procesa un voto de forma simple y directa.
     * Registra el voto localmente y lo envía al broker.
     * 
     * @param voto El voto a procesar
     * @param votante El votante que emitio el voto (para marcar como votado)
     * @throws IllegalArgumentException si los datos son invalidos
     * @throws RuntimeException si hay error en el procesamiento
     */
    public void procesarVoto(Voto voto, Ciudadano votante) {
        if (voto == null) {
            throw new IllegalArgumentException("El voto no puede ser null");
        }
        if (votante == null) {
            throw new IllegalArgumentException("El votante no puede ser null");
        }
        
        try {
            // 1. Marcar votante como votado (operacion local)
            votante.marcarComoVotado();
            
            // 2. Registrar voto en repositorio EN MEMORIA
            repositorio.registrarVoto(voto);
            
            // 3. Enviar voto al broker (solo el voto)
            enviarVoto(voto);
            
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {            
            throw new RuntimeException("Error procesando voto: " + e.getMessage());
        } finally {
            votante.setYaVoto(false);
        }
    }
      /**
     * Envía un voto al broker de forma simple y silenciosa.
     * 
     * @param voto El voto a enviar
     */
    private void enviarVoto(Voto voto) {
        try {
            servicioIce.enviarVoto(voto);
            // Envio completamente silencioso
        } catch (Exception e) {
            // Solo errores críticos de conexión
            System.err.println("Error de conexión enviando voto: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene el repositorio de mesa de votacion asociado.
     * 
     * @return Repositorio de mesa de votacion
     */
    public RepositorioMesaVotacion getRepositorio() {
        return repositorio;
    }
    
    /**
     * Detiene el coordinador (método mantenido para compatibilidad).
     */
    public void detener() {
        System.out.println("Coordinador de envio detenido.");
    }
}