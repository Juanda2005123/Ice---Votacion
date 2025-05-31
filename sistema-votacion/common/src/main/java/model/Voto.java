package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Voto {
    private Candidato candidato;
    private LocalDateTime fechaHora;
    private String mesaId;
    private String votoId;

    public Voto(String votoId, Candidato candidato, LocalDateTime fechaHora, String mesaId) {
        this.votoId = votoId;
        this.candidato = candidato;
        this.fechaHora = fechaHora;
        this.mesaId = mesaId;
    }

    public String getVotoId() {
        return votoId;
    }
    
    public Candidato getCandidato() {
        return candidato;
    }
    
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
    
    public String getMesaId() {
        return mesaId;
    }
    
    public String getFechaHoraFormateada() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return fechaHora.format(formatter);
    }
    
    @Override
    public String toString() {
        return String.format("Voto{candidato='%s', fecha='%s', mesa='%s'}", 
                           candidato.getNombreCompleto(), getFechaHoraFormateada(), mesaId);
    }
}
