package model;

public class Departamento {
    private Integer id;
    private String nombre;
    
    // Constructor
    public Departamento(Integer id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }
    
    // Default constructor
    public Departamento() {
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
}
