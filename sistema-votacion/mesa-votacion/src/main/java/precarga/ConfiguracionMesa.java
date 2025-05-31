package precarga;

import model.Candidato;
import model.Votante;
import java.util.List;

/**
 * Clase que encapsula la configuracion inicial de una mesa de votacion.
 * Contiene todos los datos necesarios para precargar una mesa.
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-05-30
 */
public class ConfiguracionMesa {
    
    private String idMesa;
    private List<Candidato> candidatos;
    private List<Votante> votantesElegibles;
    
    /**
     * Constructor para configuracion de mesa.
     * 
     * @param idMesa ID de la mesa de votacion
     * @param candidatos Lista de candidatos disponibles
     * @param votantesElegibles Lista de votantes asignados a esta mesa
     */
    public ConfiguracionMesa(String idMesa, List<Candidato> candidatos, List<Votante> votantesElegibles) {
        this.idMesa = idMesa;
        this.candidatos = candidatos;
        this.votantesElegibles = votantesElegibles;
    }
    
    // Getters y Setters
    public String getIdMesa() { return idMesa; }
    public void setIdMesa(String idMesa) { this.idMesa = idMesa; }
    
    public List<Candidato> getCandidatos() { return candidatos; }
    public void setCandidatos(List<Candidato> candidatos) { this.candidatos = candidatos; }
    
    public List<Votante> getVotantesElegibles() { return votantesElegibles; }
    public void setVotantesElegibles(List<Votante> votantesElegibles) { this.votantesElegibles = votantesElegibles; }
    
    /**
     * Valida que la configuracion sea correcta.
     * 
     * @throws IllegalArgumentException si hay datos invalidos
     */
    public void validar() {
        if (idMesa == null || idMesa.trim().isEmpty()) {
            throw new IllegalArgumentException("ID de mesa no puede ser null o vacio");
        }
        
        if (candidatos == null || candidatos.isEmpty()) {
            throw new IllegalArgumentException("La lista de candidatos no puede ser null o vacia");
        }
        
        if (votantesElegibles == null || votantesElegibles.isEmpty()) {
            throw new IllegalArgumentException("La lista de votantes no puede ser null o vacia");
        }
        
        // Validar que todos los votantes pertenezcan a esta mesa
        for (Votante votante : votantesElegibles) {
            if (!idMesa.equals(votante.getMesaId())) {
                throw new IllegalArgumentException("Votante " + votante.getCedula() + " no pertenece a la mesa " + idMesa);
            }
        }
    }
}