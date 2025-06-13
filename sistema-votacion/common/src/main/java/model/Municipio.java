package model;

public class Municipio {
    private Integer id;
    private String nombre;
    private Integer departamentoId;

    public Municipio(Integer id, String nombre, Integer departamentoId) {
        this.id = id;
        this.nombre = nombre;
        this.departamentoId = departamentoId;
    }

    public Integer getId() {
        return id;
    }
    public String getNombre() {
        return nombre;
    }
    public Integer getDepartamentoId() {
        return departamentoId;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public void setDepartamentoId(Integer departamentoId) {
        this.departamentoId = departamentoId;
    }
    

}
