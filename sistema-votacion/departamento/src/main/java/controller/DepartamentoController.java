package controller;

import model.Voto;
import config.ConfiguracionDepartamento;
import comunicacion.ServicioComunicacionDepartamento;
import comunicacion.ServicioVerificacionConectividad;

/**
 * Controlador principal del departamento de votacion que maneja el reenvio de votos.
 * 
 * Este controlador es responsable de:
 * - Recibir votos desde el broker lugar-departamento
 * - Verificar conectividad con el broker destino (servidor central)
 * - Reenviar votos al servidor central (por implementar)
 * 
 * El controlador no realiza validaciones de negocio ni almacenamiento,
 * solo actua como intermediario en la cadena de comunicacion.
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-06-15
 */
public class DepartamentoController {
    
    private ServicioComunicacionDepartamento comunicacion;
    private ServicioVerificacionConectividad verificador;
    private ConfiguracionDepartamento config;

    /**
     * Constructor que inicializa el controlador con la configuracion del departamento.
     * 
     * @param config Configuracion del departamento con destino y parametros
     */
    public DepartamentoController(ConfiguracionDepartamento config) {        
        this.config = config;
        this.comunicacion = new ServicioComunicacionDepartamento(config);
        this.verificador = new ServicioVerificacionConectividad();
    }
      /**
     * Verifica la conectividad con el servidor central al iniciar el departamento de votacion.
     * Muestra informacion detallada del servidor central.
     */
    public void verificarConectividadInicial() {
        System.out.println("=== VERIFICANDO CONECTIVIDAD CON SERVIDOR CENTRAL ===");
        
        boolean conectado = verificador.verificarConectividad(config);
        if (conectado) {
            // Mostrar informacion detallada del servidor central conectado
            System.out.println("[OK] Conexion exitosa con servidor central en " + 
                             config.getServidorCentralHost() + ":" + config.getServidorCentralPuerto());
        } else {
            // Mostrar advertencia para servidor central no conectado
            System.out.println("[!] ADVERTENCIA: No se pudo conectar con servidor central en " + 
                             config.getServidorCentralHost() + ":" + config.getServidorCentralPuerto());
        }
        
        System.out.println("=== VERIFICACION DE CONECTIVIDAD COMPLETADA ===");
    }
    
    /**
     * Funcion principal que recibe un voto y lo reenvia al broker destino.
     * No realiza validaciones de negocio ni almacenamiento, solo reenvio.
     * 
     * @param voto Voto a procesar y reenviar
     * @return true si el voto fue reenviado exitosamente, false en caso contrario
     */
    public boolean procesarVoto(Voto voto) {
        System.out.println(voto.getCandidato().getNombre());
        // Solo reenviar el voto
        return comunicacion.reenviarVoto(voto);
    }       
    /**
     * Valida un voto - NO IMPLEMENTADO EN DEPARTAMENTO.
     * El departamento no realiza validaciones de ciudadanos, solo reenvio de votos.
     * 
     * @param documento Documento del votante como String
     * @param candidatoId ID del candidato elegido
     * @return No retorna, lanza excepcion
     * @throws UnsupportedOperationException Siempre, ya que no se utiliza en departamento
     */
    public int validarVoto(String documento, Integer candidatoId) {
        throw new UnsupportedOperationException("La validacion de ciudadanos no se implementa en el departamento. Los votos se reenvian directamente al servidor central.");
    }
    
    /**
     * Verifica conectividad con el servidor central.
     * Util para diagnostico y monitoreo del estado de la conexion.
     */
    public void verificarEstadoDestinos() {
        verificador.verificarConectividadDestino(config);
    }
    
    /**
     * Verifica conectividad con el servidor central.
     * Metodo de compatibilidad para verificacion directa.
     * 
     * @return true si la conexion es exitosa, false en caso contrario
     */
    public boolean verificarConectividadServidor() {
        return verificador.verificarConectividad(config);
    }
    
    /**
     * Cierra todas las conexiones y libera recursos.
     * Debe llamarse al finalizar el uso del controlador.
     */
    public void cerrar() {
        if (verificador != null) {
            verificador.cerrar();
        }
        if (comunicacion != null) {
            comunicacion.cerrarConexion();
        }
        System.out.println("DepartamentoController " + config.getDepartamentoId() + " cerrado");
    }
}
