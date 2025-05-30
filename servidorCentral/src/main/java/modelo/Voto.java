package modelo;

import java.time.LocalDateTime;

public class Voto {
    private String id;
    private String cedula;
    private String candidato;
    private String mesaId;
    private LocalDateTime timestamp;

    public Voto(String id, String cedula, String candidato, String mesaId) {
        this.id = id;
        this.cedula = cedula;
        this.candidato = candidato;
        this.mesaId = mesaId;
        this.timestamp = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }
    public String getCedula() {
        return cedula;
    }
    public String getCandidato() {
        return candidato;
    }
    public String getMesaId() {
        return mesaId;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

}
