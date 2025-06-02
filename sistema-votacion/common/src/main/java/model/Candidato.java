package model;

/**
 * Representa un candidato en el sistema de votación
 */
public class Candidato {
    private String id;
    private String cedula;
    private String nombre;
    private String apellido;
    private String partidoPolitico;
    
    public Candidato(String id, String cedula, String nombre, String apellido, String partidoPolitico) {
        this.id = id;
        this.cedula = cedula;
        this.nombre = nombre;
        this.apellido = apellido;
        this.partidoPolitico = partidoPolitico;
    }
    
    public Candidato(String id, String nombre, String partidoPolitico) {
        this.id = id;
        this.cedula = id; // Use id as cedula for backward compatibility
        this.nombre = nombre;
        this.apellido = ""; // Default empty apellido
        this.partidoPolitico = partidoPolitico;
    }
      // Constructor para voto en blanco o candidatos especiales
    public Candidato(String id, String nombre) {
        this(id, id, nombre, "", "NA"); // Use id as cedula, empty apellido
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public String getCedula() {
        return cedula;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public String getApellido() {
        return apellido;
    }
    
    public String getPartidoPolitico() {
        return partidoPolitico;
    }    // Setters
    public void setId(String id) {
        this.id = id;
    }
    
    public void setCedula(String cedula) {
        this.cedula = cedula;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public void setApellido(String apellido) {
        this.apellido = apellido;
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
