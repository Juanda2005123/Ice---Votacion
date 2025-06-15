package votos;

import comunicacion.ServicioComunicacionIce;

/**
 * Clase responsable de verificar y validar votos antes de ser enviados.
 * Coordina entre validaciones locales y remotas.
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-06-14
 */
public class VerificacionVoto {
    private RepositorioMesaVotacion repositorio;
    private ServicioComunicacionIce servicioIce;

    /**
     * Constructor que inicializa la verificacion con repositorio local y servicio remoto.
     * 
     * @param repositorio Repositorio local de la mesa
     * @param servicioIce Servicio para comunicacion remota
     */
    public VerificacionVoto(RepositorioMesaVotacion repositorio, ServicioComunicacionIce servicioIce) {
        this.servicioIce = servicioIce;
        this.repositorio = repositorio;
    }

    /**
     * Valida un voto realizando verificaciones locales y remotas.
     * 
     * @param documento Documento del votante
     * @param candidatoId ID del candidato elegido
     * @return Código de validación: 0=puede votar, 1=no es su mesa, 2=ya votó, 3=no existe
     */
    public Integer validarVoto(String documento, Integer candidatoId) {
        try {
            // Primero verificar localmente
            int validacionLocal = repositorio.validarMesaYVoto(documento);
            
            if (validacionLocal == 0 || validacionLocal == 2) {
                return validacionLocal; // Retorna el estado de validacion local
            } else {
                // Si no está en la mesa local, consultar remotamente
                return servicioIce.validarVoto(documento, candidatoId);
            }
        } catch (Exception e) {
            // Solo errores críticos de conexión
            System.err.println("Error de conexion validando voto: " + e.getMessage());
            return 3; // Error se considera como "no existe"
        }
    }
}
