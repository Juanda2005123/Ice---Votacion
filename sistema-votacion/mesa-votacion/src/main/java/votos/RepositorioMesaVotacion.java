package votos;

import model.Voto;
import model.Candidato;
import model.Ciudadano;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Repositorio central para toda la informacion de la mesa de votacion.
 * Se encarga del almacenamiento y gestion de votos, votantes y candidatos EN MEMORIA.
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
    private Map<String, Ciudadano> votantesElegibles;     // Mapa por documento para busqueda O(1)
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
    public Candidato obtenerCandidatoPorId(Integer idCandidato) {
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
    public void cargarVotantesElegibles(List<Ciudadano> votantes) {
        if (votantes == null || votantes.isEmpty()) {
            throw new IllegalArgumentException("La lista de votantes no puede ser null o vacia");
        }
        
        this.votantesElegibles.clear();
        for (Ciudadano votante : votantes) {
            if (votante.getDocumento() == null) {
                throw new IllegalArgumentException("Votante con documento null encontrado");
            }
            this.votantesElegibles.put(votante.getDocumento(), votante);
        }
    }
    
    /**
     * Obtiene un votante por su documento.
     * 
     * @param documento Numero de documento del votante
     * @return Votante encontrado o null
     */
    public Ciudadano obtenerVotantePorDocumento(String documento) {
        return votantesElegibles.get(documento);
    }
    
    /**
     * Verifica si un votante es elegible para esta mesa.
     * 
     * @param documento Numero de documento del votante
     * @return true si es elegible
     */
    public boolean esVotanteElegible(String documento) {
        return votantesElegibles.containsKey(documento);
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
                .filter(Ciudadano::isYaVoto)
                .count();
    }
      /**
     * Obtiene todos los votantes elegibles.
     * 
     * @return Coleccion inmutable de votantes elegibles
     */
    public Collection<Ciudadano> getVotantesElegibles() {
        return Collections.unmodifiableCollection(votantesElegibles.values());
    }
    
    /**
     * Obtiene una lista de ciudadanos disponibles para simulacion.
     * Devuelve todos los ciudadanos elegibles como lista para facilitar
     * la seleccion aleatoria durante la simulacion.
     * 
     * @return Lista de ciudadanos elegibles para simulacion
     */
    public List<Ciudadano> obtenerCiudadanosParaSimulacion() {
        return new ArrayList<>(votantesElegibles.values());
    }
    
    // ===== GESTION DE VOTOS =====
    
    /**
     * Registra un nuevo voto en el repositorio EN MEMORIA.
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
        
        try {
            votosRegistrados.add(voto);
        } catch (Exception e) {
            throw new RuntimeException("Error almacenando voto en repositorio: " + e.getMessage());
        }
    }

    public int validarMesaYVoto(String documento) {
        Ciudadano ciudadano = votantesElegibles.get(documento);
        
        if (ciudadano == null) {
            return -1;
        }
        
        if (ciudadano.isYaVoto()) {
            return 2;
        }
        
        return 0;
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