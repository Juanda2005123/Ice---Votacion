package model;

public class MesaVotacion {
    private Integer id;
    private Integer consecutive;
    private Integer puestoId;
    
    // Constructor
    public MesaVotacion(Integer id, Integer consecutive, Integer puestoId) {
        this.id = id;
        this.consecutive = consecutive;
        this.puestoId = puestoId;
    }
    
    // Default constructor
    public MesaVotacion() {
    }
    
    // Getter and Setter for id
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    // Getter and Setter for consecutive
    public Integer getConsecutive() {
        return consecutive;
    }
    
    public void setConsecutive(Integer consecutive) {
        this.consecutive = consecutive;
    }
    
    // Getter and Setter for puestoId
    public Integer getPuestoId() {
        return puestoId;
    }
    
    public void setPuestoId(Integer puestoId) {
        this.puestoId = puestoId;
    }
}
