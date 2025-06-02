package votos;

import model.Voto;
import model.Votante;
import comunicacion.ServicioComunicacionIce;

/**
 * Coordinador de envio de votos que maneja la logica de envio al servidor central.
 * Coordina entre el repositorio local y el servicio de comunicacion Ice.
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-05-30
 */
public class CoordinadorEnvioVotos {
    
    private RepositorioMesaVotacion repositorio;
    private ServicioComunicacionIce servicioIce;
    private ColaMensajesConfiables colaConfiable;
    private String mesaId;
    private volatile boolean continuar = true;
    private Thread hiloReintento;
    
    /**
     * Constructor del Coordinador de Envio de Votos.
     * 
     * @param repositorio Repositorio de mesa de votacion
     */
    public CoordinadorEnvioVotos(RepositorioMesaVotacion repositorio) {
        if (repositorio == null) {
            throw new IllegalArgumentException("El repositorio no puede ser null");
        }
        
        this.repositorio = repositorio;
        this.servicioIce = new ServicioComunicacionIce(); 
        this.colaConfiable = new ColaMensajesConfiables();
        this.mesaId = repositorio.getIdMesaVotacion();

        iniciarHiloReintentos();
    }
    
    /**
     * Procesa el registro de un voto completo (voto + votante).
     * Esta operacion es atomica: o se completa todo o falla todo.
     * 
     * @param voto El voto a procesar
     * @param votante El votante que emitio el voto
     * @throws IllegalArgumentException si los datos son invalidos
     * @throws RuntimeException si hay error en el procesamiento
     */
    public void procesarVotoCompleto(Voto voto, Votante votante) {
        if (voto == null) {
            throw new IllegalArgumentException("El voto no puede ser null");
        }
        if (votante == null) {
            throw new IllegalArgumentException("El votante no puede ser null");
        }
        
        // Verificar que el votante no haya votado ya
        if (votante.isYaVoto()) {
            throw new IllegalArgumentException("El votante ya ha emitido su voto");
        }
        
        try {
            // 1. Marcar votante como votado (operacion local)
            votante.marcarComoVotado();
            
            // 2. Registrar voto en repositorio
            repositorio.registrarVoto(voto);
            
            // 3. Enviar al servidor (cuando Ice este implementado)
            enviarVotoCompletoAServidor(voto, votante);
            
        } catch (IllegalArgumentException e) {
            // En caso de error de validacion, revertir estado del votante
            votante.desmarcarVoto();
            throw e;
        } catch (Exception e) {
            // En caso de cualquier otro error, revertir estado del votante
            votante.desmarcarVoto();
            throw new RuntimeException("Error procesando voto completo: " + e.getMessage());
        }
    }
    
    /**
     * Envia un voto completo (voto + votante) al servidor central.
     * 
     * @param voto El voto a enviar
     * @param votante El votante que voto
     * @throws RuntimeException si hay error en el envio
     */
    private void enviarVotoCompletoAServidor(Voto voto, Votante votante) {
        try {
            boolean ackRecibido = servicioIce.enviarVotoVotanteConACK(mesaId, voto, votante);
            if (!ackRecibido) {
                System.err.println("No se recibió confirmación del servidor. Encolando voto.");
                colaConfiable.encolar(voto, votante);
            }
        } catch (Exception e) {
            System.err.println("Error enviando voto: " + e.getMessage() + " - Encolando para reintento.");
            colaConfiable.encolar(voto, votante);
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

    private void iniciarHiloReintentos() {
        hiloReintento = new Thread(() -> {
            while (continuar) {
                try {
                    Thread.sleep(5000); // cada 5 segundos
                    ColaMensajesConfiables.EntradaVoto entrada = colaConfiable.obtenerSiguiente();
                    if (entrada != null) {
                        boolean exito = servicioIce.enviarVotoVotanteConACK(mesaId, entrada.voto, entrada.votante);
                        if (!exito) {
                            colaConfiable.encolar(entrada.voto, entrada.votante);
                        } else {
                            System.out.println("Reintento exitoso del voto: " + entrada.voto.getVotoId());
                        }
                    }
                } catch (InterruptedException e) {
                    System.err.println("Hilo de reintento interrumpido.");
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error en reintento: " + e.getMessage());
                }
            }
        }, "HiloReintentoReliableMessaging");

        hiloReintento.start();
    }


    public void detener() {
        continuar = false;
        if (hiloReintento != null) {
            hiloReintento.interrupt();
        }
        System.out.println("Hilo de reintento detenido.");
    }

    

}