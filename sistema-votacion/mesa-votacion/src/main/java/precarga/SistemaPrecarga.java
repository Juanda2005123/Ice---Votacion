package precarga;

import model.Candidato;
import model.Ciudadano;
import votos.RepositorioMesaVotacion;
import java.util.ArrayList;
import java.util.List;

/**
 * Sistema de precarga de datos para mesa de votacion.
 * Se encarga de procesar y validar datos de configuracion antes de cargarlos.
 * 
 * Responsabilidades:
 * - Procesar configuracion recibida del servidor central
 * - Validar integridad de datos de precarga
 * - Cargar datos en el repositorio de manera controlada
 * - Manejar errores de precarga y rollback
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-05-30
 */
public class SistemaPrecarga {
    
    private RepositorioMesaVotacion repositorio;
    private boolean precargaCompletada;
    
    /**
     * Constructor del sistema de precarga.
     * 
     * @param repositorio Repositorio donde cargar los datos
     */
    public SistemaPrecarga(RepositorioMesaVotacion repositorio) {
        if (repositorio == null) {
            throw new IllegalArgumentException("El repositorio no puede ser null");
        }
        
        this.repositorio = repositorio;
        this.precargaCompletada = false;
    }
    
    /**
     * Realiza la precarga completa de la mesa con configuracion recibida.
     * Operacion atomica: si algo falla, no se carga nada.
     * 
     * @param configuracion Configuracion de mesa recibida del servidor
     * @throws IllegalArgumentException si la configuracion es invalida
     * @throws RuntimeException si hay error en la precarga
     */
    public void precargarMesa(ConfiguracionMesa configuracion) {
        if (configuracion == null) {
            throw new IllegalArgumentException("La configuracion no puede ser null");
        }
        
        if (precargaCompletada) {
            throw new RuntimeException("La mesa ya ha sido precargada. No se permite recargar.");
        }
        
        try {
            // 1. Validar configuracion
            configuracion.validar();
            
            // 2. Verificar que sea la mesa correcta
            if (!repositorio.getIdMesaVotacion().equals(configuracion.getIdMesa())) {
                throw new IllegalArgumentException("La configuracion no corresponde a esta mesa de votacion");
            }
            
            // 3. Precargar candidatos
            precargarCandidatos(configuracion.getCandidatos());
            
            // 4. Precargar votantes
            precargarVotantes(configuracion.getVotantesElegibles());
            
            // 5. Marcar precarga como completada
            this.precargaCompletada = true;
            
        } catch (Exception e) {
            // En caso de error, limpiar cualquier dato parcialmente cargado
            throw new RuntimeException("Error durante la precarga: " + e.getMessage());
        }
    }
    
    /**
     * Precarga la lista de candidatos con validaciones adicionales.
     * 
     * @param candidatos Lista de candidatos a precargar
     * @throws IllegalArgumentException si hay candidatos invalidos
     */
    private void precargarCandidatos(List<Candidato> candidatos) {
        // Validaciones adicionales especificas para candidatos
        List<Integer> idsUnicos = new ArrayList<>();
        
        for (Candidato candidato : candidatos) {
            if (candidato.getId() == null) {
                throw new IllegalArgumentException("Candidato con ID invalido encontrado");
            }
            
            if (candidato.getNombre() == null || candidato.getNombre().trim().isEmpty()) {
                throw new IllegalArgumentException("Candidato sin nombre encontrado: " + candidato.getId());
            }
            
            // Verificar IDs unicos
            if (idsUnicos.contains(candidato.getId())) {
                throw new IllegalArgumentException("ID de candidato duplicado: " + candidato.getId());
            }
            idsUnicos.add(candidato.getId());
        }
        
        // Verificar que exista al menos un candidato normal
        if (candidatos.isEmpty()) {
            throw new IllegalArgumentException("Debe existir al menos un candidato");
        }
        
        // Cargar en repositorio
        repositorio.cargarCandidatos(candidatos);
    }
    
    /**
     * Precarga la lista de votantes con validaciones adicionales.
     * 
     * @param votantes Lista de votantes a precargar
     * @throws IllegalArgumentException si hay votantes invalidos
     */
    private void precargarVotantes(List<Ciudadano> votantes) {
        // Validaciones adicionales especificas para votantes
        List<String> documentosUnicos = new ArrayList<>();
        
        for (Ciudadano votante : votantes) {
            // Usar getDocumento() en lugar de getCedula()
            if (votante.getDocumento() == null || votante.getDocumento().trim().isEmpty()) {
                throw new IllegalArgumentException("Votante con documento invalido encontrado");
            }
            
            if (votante.getNombre() == null || votante.getNombre().trim().isEmpty()) {
                throw new IllegalArgumentException("Votante sin nombre encontrado: " + votante.getDocumento());
            }
            
            // Usar getApellido() en lugar de getApellidos()
            if (votante.getApellido() == null || votante.getApellido().trim().isEmpty()) {
                throw new IllegalArgumentException("Votante sin apellido encontrado: " + votante.getDocumento());
            }
            
            // Verificar documentos unicos
            if (documentosUnicos.contains(votante.getDocumento())) {
                throw new IllegalArgumentException("Documento duplicado encontrado: " + votante.getDocumento());
            }
            documentosUnicos.add(votante.getDocumento());
            
            // Verificar que pertenezca a esta mesa
            if (!repositorio.getIdMesaVotacion().equals(votante.getMesaId())) {
                throw new IllegalArgumentException("Votante no pertenece a esta mesa: " + votante.getDocumento());
            }
        }
        
        // Cargar en repositorio
        repositorio.cargarVotantesElegibles(votantes);
    }
    
    /**
     * Genera configuracion de datos simulados para pruebas locales.
     * 
     * @param idMesa ID de la mesa para la cual generar datos
     * @return Configuracion con datos simulados
     */
    public ConfiguracionMesa generarConfiguracionSimulada(String idMesa) {
        // Generar candidatos simulados
        List<Candidato> candidatos = new ArrayList<>();
        candidatos.add(new Candidato(1, "Juan Carlos Perez", "Partido Liberal"));
        candidatos.add(new Candidato(2, "Maria Elena Gonzalez", "Partido Conservador"));
        candidatos.add(new Candidato(3, "Roberto Sanchez Diaz", "Partido Verde"));
        candidatos.add(new Candidato(4, "Ana Maria Torres", "Movimiento Ciudadano"));
        candidatos.add(new Candidato(5, "Carlos Eduardo Ramirez", "Partido de la Unidad"));
        candidatos.add(new Candidato(99, "Voto en Blanco", "BLANCO"));
        
        // Generar votantes simulados para esta mesa
        List<Ciudadano> votantes = new ArrayList<>();
        votantes.add(new Ciudadano(1, "12345678", "Ana", "Garcia Lopez", idMesa));
        votantes.add(new Ciudadano(2, "23456789", "Carlos", "Rodriguez Perez", idMesa));
        votantes.add(new Ciudadano(3, "34567890", "Maria", "Fernandez Torres", idMesa));
        votantes.add(new Ciudadano(4, "45678901", "Jose", "Martinez Ramirez", idMesa));
        votantes.add(new Ciudadano(5, "56789012", "Laura", "Gonzalez Diaz", idMesa));
        votantes.add(new Ciudadano(6, "67890123", "Pedro", "Hernandez Silva", idMesa));
        votantes.add(new Ciudadano(7, "1058932648", "Juan David", "Quintero Peña", idMesa));
        votantes.add(new Ciudadano(8, "1109663632", "Mariana", "De La Cruz Posso", idMesa));
        votantes.add(new Ciudadano(9, "89012345", "Miguel", "Castro Vargas", idMesa));
        votantes.add(new Ciudadano(10, "90123456", "Elena", "Ruiz Mendoza", idMesa));
        votantes.add(new Ciudadano(11, "01234567", "Diego", "Jimenez Ortega", idMesa));
        
        return new ConfiguracionMesa(idMesa, candidatos, votantes);
    }
    
    /**
     * Verifica si la precarga ha sido completada.
     * 
     * @return true si la mesa esta precargada
     */
    public boolean isPrecargaCompletada() {
        return precargaCompletada;
    }
    
    /**
     * Obtiene el repositorio asociado.
     * 
     * @return Repositorio de mesa de votacion
     */
    public RepositorioMesaVotacion getRepositorio() {
        return repositorio;
    }
}