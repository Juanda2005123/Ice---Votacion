package votos;

import model.Voto;
import model.Ciudadano;
import comunicacion.ServicioComunicacionIce;
import java.util.List;
import java.util.Map;

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
        this.mesaId = repositorio.getIdMesaVotacion();

        iniciarHiloReintentos();
    }
      /**
     * Procesa el registro de un voto completo (voto + votante) CON PERSISTENCIA.
     * Esta operacion es atomica: o se completa todo o falla todo.
     * Implementa el patrón Reliable Message.
     * 
     * @param voto El voto a procesar
     * @param votante El votante que emitio el voto
     * @throws IllegalArgumentException si los datos son invalidos
     * @throws RuntimeException si hay error en el procesamiento
     */
    public void procesarVotoCompleto(Voto voto, Ciudadano votante) {
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
            
            // 2. Registrar voto en repositorio CON PERSISTENCIA (auditoria + mensajes pendientes)
            repositorio.registrarVotoCompleto(voto, votante);
            
            // 3. Enviar al servidor central
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
     * Si recibe confirmación, elimina el voto de mensajes pendientes.
     * 
     * @param voto El voto a enviar
     * @param votante El votante que voto
     * @throws RuntimeException si hay error en el envio
     */
    private void enviarVotoCompletoAServidor(Voto voto, Ciudadano votante) {
        try {
            boolean ackRecibido = servicioIce.enviarVotoVotanteConACK(mesaId, voto, votante);
            if (ackRecibido) {
                // Confirmación recibida: eliminar de mensajes pendientes
                boolean confirmado = repositorio.confirmarVotoEnviado(voto.getVotoId());
                if (confirmado) {
                    System.out.println("Voto confirmado y enviado exitosamente: " + voto.getVotoId());
                } else {
                    System.err.println("Voto enviado pero no se pudo confirmar en persistencia: " + voto.getVotoId());
                }
            } else {
                System.err.println("No se recibió confirmación del servidor para voto: " + voto.getVotoId());
                // El voto ya está en mensajes pendientes, será reintentado
            }
        } catch (Exception e) {
            System.err.println("Error enviando voto " + voto.getVotoId() + ": " + e.getMessage());
            // El voto ya está en mensajes pendientes, será reintentado
        }
    }

    
    /**
     * Obtiene el repositorio de mesa de votacion asociado.
     * 
     * @return Repositorio de mesa de votacion
     */
    public RepositorioMesaVotacion getRepositorio() {
        return repositorio;
    }    private void iniciarHiloReintentos() {
        hiloReintento = new Thread(() -> {
            while (continuar) {
                try {
                    Thread.sleep(5000); // cada 5 segundos
                    
                    // Obtener mensajes pendientes desde persistencia
                    List<PersistenciaVotos.EntradaVotoCompleta> pendientes = repositorio.obtenerMensajesPendientes();
                    
                    for (PersistenciaVotos.EntradaVotoCompleta entrada : pendientes) {
                        try {
                            boolean exito = servicioIce.enviarVotoVotanteConACK(mesaId, entrada.voto, entrada.votante);
                            if (exito) {
                                // Confirmación recibida: eliminar de mensajes pendientes
                                boolean confirmado = repositorio.confirmarVotoEnviado(entrada.voto.getVotoId());
                                if (confirmado) {
                                    System.out.println("Reintento exitoso del voto: " + entrada.voto.getVotoId());
                                }
                            }
                            // Si no hay éxito, el voto permanece en mensajes pendientes para próximo reintento
                            
                        } catch (Exception e) {
                            System.err.println("Error en reintento del voto " + entrada.voto.getVotoId() + ": " + e.getMessage());
                            // El voto permanece en mensajes pendientes para próximo reintento
                        }
                    }
                    
                } catch (InterruptedException e) {
                    System.err.println("Hilo de reintento interrumpido.");
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error en hilo de reintento: " + e.getMessage());
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
    
    /**
     * Obtiene estadísticas de persistencia para monitoreo.
     * 
     * @return Mapa con estadísticas
     */
    public Map<String, Integer> obtenerEstadisticasPersistencia() {
        return repositorio.obtenerEstadisticasPersistencia();
    }
    
    /**
     * Obtiene información de los archivos de persistencia.
     * 
     * @return Mapa con nombres de archivos
     */
    public Map<String, String> obtenerArchivos() {
        return repositorio.obtenerArchivos();
    }
    
    /**
     * Fuerza el reintento de todos los mensajes pendientes.
     * Útil para pruebas o situaciones especiales.
     */
    public void forzarReintentoMensajes() {
        List<PersistenciaVotos.EntradaVotoCompleta> pendientes = repositorio.obtenerMensajesPendientes();
        
        System.out.println("Forzando reintento de " + pendientes.size() + " mensajes pendientes...");
        
        for (PersistenciaVotos.EntradaVotoCompleta entrada : pendientes) {
            try {
                boolean exito = servicioIce.enviarVotoVotanteConACK(mesaId, entrada.voto, entrada.votante);
                if (exito) {
                    repositorio.confirmarVotoEnviado(entrada.voto.getVotoId());
                    System.out.println("Reintento manual exitoso: " + entrada.voto.getVotoId());
                }
            } catch (Exception e) {
                System.err.println("Error en reintento manual del voto " + entrada.voto.getVotoId() + ": " + e.getMessage());
            }
        }
    }

}