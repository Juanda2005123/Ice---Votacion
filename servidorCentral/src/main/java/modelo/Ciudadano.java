package modelo;

public class Ciudadano {
    private String cedula;
    private String nombre;
    private String mesaAsignada;
    private boolean haVotado;

    public Ciudadano(String cedula, String nombre, String mesaAsignada) {
        this.cedula = cedula;
        this.nombre = nombre;
        this.mesaAsignada = mesaAsignada;
        this.haVotado = false;
    }

    public void marcarComoVotado() {
        this.haVotado = true;
    }

    // Getters y setters
}
