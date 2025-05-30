package modelo;

public class Candidato {
    private String id;
    private String nombre;
    private String partido;

    public Candidato(String id, String nombre, String partido) {
        this.id = id;
        this.nombre = nombre;
        this.partido = partido;
    }
    public String getId() {
        return id;
    }
    public String getNombre() {
        return nombre;
    }
    public String getPartido() {
        return partido;
    }
    public void setId(String id) {
        this.id = id;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public void setPartido(String partido) {
        this.partido = partido;
    }
}
