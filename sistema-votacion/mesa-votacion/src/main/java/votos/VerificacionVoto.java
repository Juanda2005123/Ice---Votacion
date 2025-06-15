package votos;

import comunicacion.ServicioComunicacionIce;

public class VerificacionVoto {
    private RepositorioMesaVotacion repositorio;
    private ServicioComunicacionIce servicioIce;


    public VerificacionVoto(RepositorioMesaVotacion repositorio, ServicioComunicacionIce servicioIce) {
        this.servicioIce = servicioIce;
        this.repositorio = repositorio;
    }

    public Integer validarVoto(String documento, Integer candidatoId) {
        try {
            int valid = repositorio.validarMesaYVoto(documento);
            if (valid == 0 || valid == 2) {
                return valid; // Retorna el estado de validacion local
            } else {
                return servicioIce.validarVoto(documento, candidatoId);
            }
            // Envio completamente silencioso
        } catch (Exception e) {
            // Solo errores críticos de conexión
            System.err.println("Error de conexión enviando voto: " + e.getMessage());
        }
    }
    
}
