package modelo;

public class ConsultaCiudadano {
    public String departamento;
    public String municipio;
    public String lugar;
    public String direccion;
    public int mesa;

    public ConsultaCiudadano(String departamento, String municipio, String lugar, String direccion, int mesa) {
        this.departamento = departamento;
        this.municipio = municipio;
        this.lugar = lugar;
        this.direccion = direccion;
        this.mesa = mesa;
    }
}
