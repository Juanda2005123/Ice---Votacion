package controller;

import model.Voto;
import config.ConfiguracionServidor;

/**
 * Controlador principal del servidor central de votacion que maneja la recepcion de votos.
 * 
 * Este controlador es responsable de:
 * - Recibir votos desde el broker departamento-central
 * - Imprimir la informacion del voto recibido (candidato)
 * - Ser el destino final del flujo de votacion
 * 
 * El controlador es el punto final de la cadena de comunicacion,
 * no reenvia votos a ningun otro destino.
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-06-15
 */
public class ServidorController {
    
    private ConfiguracionServidor config;

    /**
     * Constructor que inicializa el controlador con la configuracion del servidor central.
     * 
     * @param config Configuracion del servidor central con parametros
     */
    public ServidorController(ConfiguracionServidor config) {        
        this.config = config;
    }    
    /**
     * Funcion principal que recibe un voto e imprime la informacion del candidato.
     * El servidor central es el destino final, no reenvia votos.
     * 
     * @param voto Voto a procesar e imprimir
     * @return true siempre, ya que solo imprime informacion
     */
    public boolean procesarVoto(Voto voto) {
        // Imprimir informacion del voto recibido (solo el nombre del candidato)
        System.out.println(voto.getId());
        return true;
    }
    
    /**
     * Valida un voto - NO IMPLEMENTADO EN SERVIDOR CENTRAL.
     * El servidor central solo recibe votos ya procesados y validados.
     * 
     * @param documento Documento del votante como String
     * @param candidatoId ID del candidato elegido
     * @return No retorna, lanza excepcion
     * @throws UnsupportedOperationException Siempre, ya que no se utiliza en servidor central
     */
    public int validarVoto(String documento, Integer candidatoId) {
        throw new UnsupportedOperationException("La validacion de votantes no se implementa en el servidor central. Solo se reciben votos ya procesados.");
    }
}
