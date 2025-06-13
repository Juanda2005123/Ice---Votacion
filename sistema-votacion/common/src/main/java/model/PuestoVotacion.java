package model;

public class PuestoVotacion {
    private Integer id;
    private String nombre;
    private Integer consecutive;
    private String direccion;
    private Integer municipioId;
    
    // Constructor
    public PuestoVotacion(Integer id, String nombre, Integer consecutive, String direccion, Integer municipioId) {
        this.id = id;
        this.nombre = nombre;
        this.consecutive = consecutive;
        this.direccion = direccion;
        this.municipioId = municipioId;
    }
    
    // Default constructor
    public PuestoVotacion() {
    }
    
    // Getter and Setter for id
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    // Getter and Setter for nombre
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    // Getter and Setter for consecutive
    public Integer getConsecutive() {
        return consecutive;
    }
    
    public void setConsecutive(Integer consecutive) {
        this.consecutive = consecutive;
    }
    
    // Getter and Setter for direccion
    public String getDireccion() {
        return direccion;
    }
    
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    
    // Getter and Setter for municipioId
    public Integer getMunicipioId() {
        return municipioId;
    }
    
    public void setMunicipioId(Integer municipioId) {
        this.municipioId = municipioId;
    }
}
