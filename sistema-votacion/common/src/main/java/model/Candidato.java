package model;

/**
 * Representa un candidato en el sistema de votación
 */
public class Candidato {
    private Integer id;
    private String nombre;
    private String partidoPolitico;
    
    public Candidato(Integer id, String cedula, String nombre, String apellido, String partidoPolitico) {
        this.id = id;
        this.nombre = nombre;
        this.partidoPolitico = partidoPolitico;
    }
    
    public Candidato(Integer id, String nombre, String partidoPolitico) {
        this.id = id;
        this.nombre = nombre;
        this.partidoPolitico = partidoPolitico;
    }
      // Constructor para voto en blanco o candidatos especiales
    public Candidato(Integer id, String nombre) {
        this(id, String.valueOf(id), nombre, "", "NA");
    }

    // Getters
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
    
    /**
     * Verifica si es un voto especial (en blanco, nulo, etc.)
     */
    public boolean esVotoEspecial() {
        return id.equals("BLANCO");
    }
    
    @Override
    public String toString() {
        return String.format("Candidato{id='%s', nombre='%s', partido='%s'}", 
                           id, nombre, partidoPolitico);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Candidato candidato = (Candidato) obj;
        return id != null ? id.equals(candidato.id) : candidato.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
