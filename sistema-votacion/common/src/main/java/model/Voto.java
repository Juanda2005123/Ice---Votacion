package model;


public class Voto {
    private Candidato candidato;
    private Integer id;

    public Voto(Integer id, Candidato candidato) {
        this.id = id;
        this.candidato = candidato;
    }
    
    public Integer getId() {
        return id;
    }

    public Candidato getCandidato() {
        return candidato;
    }

    public void setCandidato(Candidato candidato) {
        this.candidato = candidato;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    
}
