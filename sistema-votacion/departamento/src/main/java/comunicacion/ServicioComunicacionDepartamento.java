package comunicacion;

import VotingSystem.*;
import model.Voto;
import config.ConfiguracionDepartamento;

/**
 * Servicio para ENVIAR votos al servidor central (conexion directa).
 * Solo reenvia votos, sin validaciones ni estadisticas.
 * (Consistente con ServicioComunicacionLugar)
 */
public class ServicioComunicacionDepartamento {
    
    private com.zeroc.Ice.Communicator communicator;
    private ConfiguracionDepartamento config;
    
    public ServicioComunicacionDepartamento(ConfiguracionDepartamento config) {
        this.config = config;
        
        try {
            communicator = com.zeroc.Ice.Util.initialize();
        } catch (Exception e) {
            throw new RuntimeException("Error inicializando cliente Ice: " + e.getMessage());
        }
    }    /**
     * Funcion principal: Reenvia un voto al servidor central
     * Implementa la comunicacion directa con el servidor central
     */
    public boolean reenviarVoto(Voto voto) {
        return enviarVotoAServidor(voto);
    }
      /**
     * Recibe una validacion de votante - NO IMPLEMENTADO EN DEPARTAMENTO.
     * El departamento no realiza validaciones, solo reenvio de votos.
     * 
     * @param documento Documento del votante
     * @param candidatoId ID del candidato elegido
     * @return No retorna, lanza excepcion
     * @throws UnsupportedOperationException Siempre, ya que no se utiliza en departamento
     */
    public int reenviarValidacionVotante(String documento, Integer candidatoId) {
        throw new UnsupportedOperationException("La validacion de ciudadanos no se implementa en el departamento. Los votos se reenvian directamente al servidor central.");
    }
    
    /**
     * Envia un voto al servidor central directamente.
     * 
     * @param voto Voto a enviar al servidor central
     * @return true si el voto fue enviado exitosamente, false en caso contrario
     */
    private boolean enviarVotoAServidor(Voto voto) {
        try {
            String proxyString = String.format("ReceptorVotos:tcp -h %s -p %d", 
                                             config.getServidorCentralHost(), 
                                             config.getServidorCentralPuerto());
            
            com.zeroc.Ice.ObjectPrx proxy = communicator.stringToProxy(proxyString);
            ReceptorVotosPrx receptorPrx = ReceptorVotosPrx.checkedCast(proxy);
            
            if (receptorPrx == null) {
                System.err.println("[ERROR] No se pudo conectar con servidor central");
                return false;
            }
            
            VotingSystem.Voto votoIce = convertirVotoJavaAIce(voto);
            return receptorPrx.recibirVoto(votoIce);
            
        } catch (Exception e) {
            System.err.println("[ERROR] Error enviando voto al servidor central: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Convierte un voto del formato Java al formato Ice.
     * Mantiene los tipos Integer para los IDs sin conversion adicional.
     * 
     * @param votoJava Voto en formato Java a convertir
     * @return Voto en formato Ice equivalente
     */
    private VotingSystem.Voto convertirVotoJavaAIce(Voto votoJava) {
        VotingSystem.Candidato candidatoIce = new VotingSystem.Candidato();
        candidatoIce.id = votoJava.getCandidato().getId(); // Integer directo
        candidatoIce.nombre = votoJava.getCandidato().getNombre();
        candidatoIce.partidoPolitico = votoJava.getCandidato().getPartidoPolitico();
        
        VotingSystem.Voto votoIce = new VotingSystem.Voto();
        votoIce.id = votoJava.getId(); // Integer directo
        votoIce.candidato = candidatoIce;
        
        return votoIce;
    }    
    /**
     * Cierra la conexion Ice
     */
    public void cerrarConexion() {
        if (communicator != null) {
            communicator.destroy();
        }
    }
}
