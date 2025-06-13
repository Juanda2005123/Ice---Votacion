package votos;

import model.Voto;
import model.Candidato;
import model.Ciudadano;
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
    private Map<String, Ciudadano> votantesElegibles;     // Mapa por cedula para busqueda O(1)
    private List<Candidato> candidatosDisponibles;
    private String idMesaVotacion;
    private LocalDateTime inicioVotacion;
    private PersistenciaVotos persistencia; // Nuevo campo para persistencia
    
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
        this.persistencia = new PersistenciaVotos(idMesaVotacion); // Inicializar persistencia
        
        // Cargar mensajes pendientes al inicializar
        cargarMensajesPendientesAlIniciar();
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
    public void cargarVotantesElegibles(List<Ciudadano> votantes) {
        if (votantes == null || votantes.isEmpty()) {
            throw new IllegalArgumentException("La lista de votantes no puede ser null o vacia");
        }
        
        this.votantesElegibles.clear();
        for (Ciudadano votante : votantes) {
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
    public Ciudadano obtenerVotantePorCedula(String cedula) {
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
    
    // ===== GESTION DE VOTOS =====
      /**
     * Registra un nuevo voto en el repositorio CON PERSISTENCIA.
     * Guarda el voto tanto en memoria como en los archivos de persistencia.
     * 
     * @param voto El voto a registrar
     * @param votante El votante que emitió el voto
     * @throws IllegalArgumentException si el voto es null o invalido
     * @throws RuntimeException si hay error en el almacenamiento
     */
    public void registrarVotoCompleto(Voto voto, Ciudadano votante) {
        if (voto == null) {
            throw new IllegalArgumentException("El voto no puede ser null");
        }
        
        if (votante == null) {
            throw new IllegalArgumentException("El votante no puede ser null");
        }
        
        if (voto.getCandidato() == null) {
            throw new IllegalArgumentException("El voto debe tener un candidato valido");
        }
        
        if (!idMesaVotacion.equals(voto.getMesaId())) {
            throw new IllegalArgumentException("El voto no pertenece a esta mesa de votacion");
        }
          try {
            // 1. Registrar en memoria
            votosRegistrados.add(voto);
            
            // 2. Guardar en persistencia (auditoria + mensajes pendientes)
            persistencia.guardarVotoCompleto(voto, votante, voto.getCandidato(), LocalDateTime.now());
            
        } catch (Exception e) {
            // En caso de error, remover de memoria si se había agregado
            votosRegistrados.removeIf(v -> v.getVotoId().equals(voto.getVotoId()));
            throw new RuntimeException("Error almacenando voto en repositorio: " + e.getMessage());
        }
    }
    
    /**
     * Registra un nuevo voto en el repositorio (método original mantenido para compatibilidad).
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
    
    // ===== MÉTODOS DE PERSISTENCIA (RELIABLE MESSAGE) =====
    
    /**
     * Confirma que un voto fue enviado exitosamente al servidor central.
     * Lo elimina de los mensajes pendientes pero lo mantiene en auditoría.
     * 
     * @param votoId ID del voto confirmado
     * @return true si se confirmó exitosamente
     */
    public boolean confirmarVotoEnviado(String votoId) {
        return persistencia.confirmarVotoEnviado(votoId);
    }
    
    /**
     * Obtiene todos los mensajes pendientes de envío al servidor.
     * Se usa para reenviar votos no confirmados.
     * 
     * @return Lista de votos y votantes pendientes
     */
    public List<PersistenciaVotos.EntradaVotoCompleta> obtenerMensajesPendientes() {
        return persistencia.cargarMensajesPendientes();
    }
    
    /**
     * Obtiene todos los votos del archivo de auditoría.
     * 
     * @return Lista completa de auditoría
     */
    public List<PersistenciaVotos.EntradaVotoCompleta> obtenerAuditoria() {
        return persistencia.cargarAuditoria();
    }
    
    /**
     * Obtiene estadísticas de persistencia.
     * 
     * @return Mapa con estadísticas
     */
    public Map<String, Integer> obtenerEstadisticasPersistencia() {
        return persistencia.obtenerEstadisticas();
    }
    
    /**
     * Obtiene los nombres de los archivos de persistencia.
     * 
     * @return Mapa con nombres de archivos
     */
    public Map<String, String> obtenerArchivos() {
        return persistencia.obtenerNombresArchivos();
    }
    
    /**
     * Carga mensajes pendientes al inicializar el repositorio.
     * Esto permite recuperar votos no confirmados en caso de reinicio.
     */
    private void cargarMensajesPendientesAlIniciar() {
        try {
            List<PersistenciaVotos.EntradaVotoCompleta> pendientes = persistencia.cargarMensajesPendientes();
            
            if (!pendientes.isEmpty()) {
                System.out.println("Recuperando " + pendientes.size() + " votos pendientes de confirmación...");
                
                // Los votos ya están en memoria a través de la auditoría,
                // aquí solo mostramos información de recuperación
                for (PersistenciaVotos.EntradaVotoCompleta entrada : pendientes) {
                    System.out.println("- Voto pendiente: " + entrada.voto.getVotoId() + 
                                     " (Votante: " + entrada.votante.getNombreCompleto() + ")");
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error cargando mensajes pendientes al iniciar: " + e.getMessage());
        }
    }

}