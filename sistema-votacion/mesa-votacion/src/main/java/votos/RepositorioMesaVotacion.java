package votos;

import model.Voto;
import model.Candidato;
import model.Votante;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Repositorio central para toda la informacion de la mesa de votacion.
 * Se encarga del almacenamiento y gestion de votos, votantes y candidatos.
 * 
 * Responsabilidades:
 * - Gestionar lista de votantes elegibles
 * - Administrar candidatos disponibles
 * - Almacenar votos registrados
 * - Proporcionar acceso controlado a toda la informacion de la mesa
 * - Mantener integridad y consistencia de datos
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-05-30
 */
public class RepositorioMesaVotacion {
    
    private List<Voto> votosRegistrados;
    private Map<String, Votante> votantesElegibles;     // Mapa por cedula para busqueda O(1)
    private List<Candidato> candidatosDisponibles;
    private String idMesaVotacion;
    private LocalDateTime inicioVotacion;
    
    /**
     * Constructor del Repositorio de Mesa de Votacion.
     * 
     * @param idMesaVotacion Identificador de la mesa de votacion
     */
    public RepositorioMesaVotacion(String idMesaVotacion) {
        if (idMesaVotacion == null || idMesaVotacion.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de mesa de votacion no puede ser null o vacio");
        }
        
        this.idMesaVotacion = idMesaVotacion;
        this.votosRegistrados = new ArrayList<>();
        this.votantesElegibles = new HashMap<>();
        this.candidatosDisponibles = new ArrayList<>();
        this.inicioVotacion = LocalDateTime.now();
    }
    
    // ===== GESTION DE CANDIDATOS =====
    
    /**
     * Inicializa la lista de candidatos disponibles para esta eleccion.
     * 
     * @param candidatos Lista de candidatos a cargar
     * @throws IllegalArgumentException si la lista es null o vacia
     */
    public void cargarCandidatos(List<Candidato> candidatos) {
        if (candidatos == null || candidatos.isEmpty()) {
            throw new IllegalArgumentException("La lista de candidatos no puede ser null o vacia");
        }
        
        this.candidatosDisponibles.clear();
        this.candidatosDisponibles.addAll(candidatos);
    }
    
    /**
     * Obtiene la lista de candidatos disponibles.
     * 
     * @return Lista inmutable de candidatos
     */
    public List<Candidato> getCandidatosDisponibles() {
        return Collections.unmodifiableList(candidatosDisponibles);
    }
    
    /**
     * Obtiene un candidato por su ID.
     * 
     * @param idCandidato ID del candidato
     * @return Candidato encontrado o null
     */
    public Candidato obtenerCandidatoPorId(String idCandidato) {
        return candidatosDisponibles.stream()
                .filter(c -> c.getId().equals(idCandidato))
                .findFirst()
                .orElse(null);
    }
    
    // ===== GESTION DE VOTANTES =====
    
    /**
     * Carga la lista de votantes elegibles para esta mesa.
     * 
     * @param votantes Lista de votantes elegibles
     * @throws IllegalArgumentException si la lista es null o vacia
     */
    public void cargarVotantesElegibles(List<Votante> votantes) {
        if (votantes == null || votantes.isEmpty()) {
            throw new IllegalArgumentException("La lista de votantes no puede ser null o vacia");
        }
        
        this.votantesElegibles.clear();
        for (Votante votante : votantes) {
            if (votante.getCedula() == null) {
                throw new IllegalArgumentException("Votante con cedula null encontrado");
            }
            this.votantesElegibles.put(votante.getCedula(), votante);
        }
    }
    
    /**
     * Obtiene un votante por su cedula.
     * 
     * @param cedula Numero de cedula del votante
     * @return Votante encontrado o null
     */
    public Votante obtenerVotantePorCedula(String cedula) {
        return votantesElegibles.get(cedula);
    }
    
    /**
     * Verifica si un votante es elegible para esta mesa.
     * 
     * @param cedula Numero de cedula del votante
     * @return true si es elegible
     */
    public boolean esVotanteElegible(String cedula) {
        return votantesElegibles.containsKey(cedula);
    }
    
    /**
     * Obtiene el total de votantes elegibles.
     * 
     * @return Numero total de votantes elegibles
     */
    public int getTotalVotantesElegibles() {
        return votantesElegibles.size();
    }
    
    /**
     * Obtiene el total de votantes que ya han votado.
     * 
     * @return Numero de votantes que ya votaron
     */
    public int getTotalVotantesQueYaVotaron() {
        return (int) votantesElegibles.values().stream()
                .filter(Votante::isYaVoto)
                .count();
    }
    
    /**
     * Obtiene todos los votantes elegibles.
     * 
     * @return Coleccion inmutable de votantes elegibles
     */
    public Collection<Votante> getVotantesElegibles() {
        return Collections.unmodifiableCollection(votantesElegibles.values());
    }
    
    // ===== GESTION DE VOTOS =====
    
    /**
     * Registra un nuevo voto en el repositorio.
     * 
     * @param voto El voto a registrar
     * @throws IllegalArgumentException si el voto es null o invalido
     * @throws RuntimeException si hay error en el almacenamiento
     */
    public void registrarVoto(Voto voto) {
        if (voto == null) {
            throw new IllegalArgumentException("El voto no puede ser null");
        }
        
        if (voto.getCandidato() == null) {
            throw new IllegalArgumentException("El voto debe tener un candidato valido");
        }
        
        if (!idMesaVotacion.equals(voto.getMesaId())) {
            throw new IllegalArgumentException("El voto no pertenece a esta mesa de votacion");
        }
        
        try {
            votosRegistrados.add(voto);
        } catch (Exception e) {
            throw new RuntimeException("Error almacenando voto en repositorio: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene todos los votos registrados.
     * 
     * @return Lista inmutable de votos registrados
     */
    public List<Voto> getVotosRegistrados() {
        return Collections.unmodifiableList(votosRegistrados);
    }
    
    /**
     * Obtiene el total de votos registrados.
     * 
     * @return Numero total de votos
     */
    public int getTotalVotos() {
        return votosRegistrados.size();
    }
    
    // ===== METODOS DE INFORMACION GENERAL =====
    
    /**
     * Obtiene el ID de la mesa de votacion.
     * 
     * @return ID de la mesa
     */
    public String getIdMesaVotacion() {
        return idMesaVotacion;
    }
    
    /**
     * Obtiene la fecha y hora de inicio de votacion.
     * 
     * @return Fecha y hora de inicio
     */
    public LocalDateTime getInicioVotacion() {
        return inicioVotacion;
    }
    

}