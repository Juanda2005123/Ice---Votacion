package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Voto {
    private Candidato candidato;
    private Integer id;

    public Voto(Integer id, Candidato candidato) {
        this.id = id;
        this.candidato = candidato;
    }
    
    public Candidato getCandidato() {
        return candidato;
    }
    
}
