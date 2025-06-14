package model;

/**
 * Representa un candidato en el sistema de votación
 */
public class Candidato {
    private Integer id;
    private String nombre;
    private String partidoPolitico;
    
    public Candidato(Integer id, String nombre, String partidoPolitico) {
        this.id = id;
        this.nombre = nombre;
        this.partidoPolitico = partidoPolitico;
    }
    public Integer getId() {
        return id;
    }
    
    public String getNombre() {
        return nombre;
    }

    public String getPartidoPolitico() {
        return partidoPolitico;
    }    // Setters
    public void setId(Integer id) {
        this.id = id;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public void setPartidoPolitico(String partidoPolitico) {
        this.partidoPolitico = partidoPolitico;
    }
    
    /**
     * Devuelve el nombre completo del candidato con su partido
     */
    public String getNombreCompleto() {
        return nombre;
    }
}
