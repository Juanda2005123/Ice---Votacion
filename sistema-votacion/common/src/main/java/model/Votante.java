package model;

/**
 * Representa un votante habilitado para votar en una mesa específica
 */
public class Votante {
    private String cedula;
    private String nombre;
    private String apellidos;
    private String apellido; // For Ice compatibility
    private String departamento;
    private String ciudad;
    private String mesaId;
    private String lugarVotacion;
    private boolean yaVoto;
      // Constructor completo
    public Votante(String cedula, String nombre, String apellidos, String departamento, String ciudad, String mesaId, String lugarVotacion) {
        this.cedula = cedula;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.apellido = apellidos; // Use apellidos as apellido for Ice compatibility
        this.departamento = departamento;
        this.ciudad = ciudad;
        this.mesaId = mesaId;
        this.yaVoto = false;
        this.lugarVotacion = lugarVotacion;
    }
    
    // Constructor completo
    public Votante(String cedula, String nombre, String apellidos, String mesaId, String lugarVotacion) {
        this.cedula = cedula;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.apellido = apellidos; // Use apellidos as apellido for Ice compatibility
        this.departamento = ""; // Default empty
        this.ciudad = ""; // Default empty
        this.mesaId = mesaId;
        this.yaVoto = false;
        this.lugarVotacion = lugarVotacion;
    }
      public Votante(String cedula, String nombre, String apellidos, String mesaId) {
        this.cedula = cedula;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.apellido = apellidos; // Use apellidos as apellido for Ice compatibility
        this.departamento = ""; // Default empty
        this.ciudad = ""; // Default empty
        this.mesaId = mesaId;
        this.yaVoto = false;
    }
    
    // Getters
    public String getCedula() {
        return cedula;
    }
    
    public String getNombre() {
        return nombre;
    }
      public String getApellidos() {
        return apellidos;
    }
    
    public String getApellido() {
        return apellido;
    }
    
    public String getDepartamento() {
        return departamento;
    }
    
    public String getCiudad() {
        return ciudad;
    }
    
    public String getMesaId() {
        return mesaId;
    }
    
    public boolean isYaVoto() {
        return yaVoto;
    }
    
    // Setters
    public void setCedula(String cedula) {
        this.cedula = cedula;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
      public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
        this.apellido = apellidos; // Keep both in sync
    }
    
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }
    
    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }
    
    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }
    
    public void setMesaId(String mesaId) {
        this.mesaId = mesaId;
    }
    
    public void setYaVoto(boolean yaVoto) {
        this.yaVoto = yaVoto;
    }
    
    /**
     * Devuelve el nombre completo del votante
     */
    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }
    
    /**
     * Marca al votante como que ya ejerció su derecho al voto
     */
    public void marcarComoVotado() {
        this.yaVoto = true;
    }

    /**
     * Marca al votante como que no ha ejercido su derecho al voto
     */
    public void desmarcarVoto() {
        this.yaVoto = false;
    }
    
    /**
     * Verifica si el votante puede votar en la mesa especificada
     */
    public boolean puedeVotarEnMesa(String mesaId) {
        return this.mesaId.equals(mesaId) && !this.yaVoto;
    }
    
    /**
     * Verifica si el votante está asignado a una mesa específica
     */
    public boolean estaAsignadoAMesa(String mesaId) {
        return this.mesaId.equals(mesaId);
    }
    
    /**
     * Obtiene el estado del votante como texto
     */
    public String getEstadoTexto() {
        if (yaVoto) {
            return "YA VOTO";
        } else {
            return "HABILITADO";
        }
    }
    
    @Override
    public String toString() {
        return String.format("Votante{cedula='%s', nombre='%s', mesa='%s', yaVoto=%s}", 
                           cedula, getNombreCompleto(), mesaId, yaVoto);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Votante votante = (Votante) obj;
        return cedula != null ? cedula.equals(votante.cedula) : votante.cedula == null;
    }
    
    @Override
    public int hashCode() {
        return cedula != null ? cedula.hashCode() : 0;
    }
}
