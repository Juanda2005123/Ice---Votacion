package model;

/**
 * Representa un votante habilitado para votar en una mesa específica
 */
public class Ciudadano {
    private Integer id;
    private String documento;
    private String nombre;
    private String apellido;
    private String mesaId;
    private boolean yaVoto;
    
    public Ciudadano(Integer id, String documento, String nombre, String apellido, String mesaId) {
        this.id = id;
        this.documento = documento;
        this.nombre = nombre;
        this.apellido = apellido;
        this.mesaId = mesaId;
        this.yaVoto = false; // Por defecto, el votante no ha votado
    }

    public String getNombre() {
        return nombre;
    }
    
    public String getApellido() {
        return apellido;
    }
    
    public String getMesaId() {
        return mesaId;
    }
    
    public boolean isYaVoto() {
        return yaVoto;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }
    
    public void setMesaId(String mesaId) {
        this.mesaId = mesaId;
    }
    
    public void setYaVoto(boolean yaVoto) {
        this.yaVoto = yaVoto;
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
}
