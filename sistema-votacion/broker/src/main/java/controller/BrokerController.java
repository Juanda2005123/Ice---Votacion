package controller;

import model.Voto;
import config.ConfiguracionBroker;
import comunicacion.ServicioComunicacionBroker;
import comunicacion.ServicioVerificacionConectividad;
import enrutamiento.EstrategiaEnrutamiento;

import java.util.concurrent.*;

/**
 * Controlador principal del broker que maneja el reenvio de votos y consultas.
 * Ahora soporta procesamiento concurrente mediante ThreadPool.
 */
public class BrokerController {


    private final ServicioComunicacionBroker comunicacion;
    private final ServicioVerificacionConectividad verificador;
    private final ConfiguracionBroker config;
    private final ExecutorService threadPool;

    public BrokerController(ConfiguracionBroker config) {
        this.config = config;
        this.comunicacion = new ServicioComunicacionBroker(config);
        this.verificador = new ServicioVerificacionConectividad();
        this.estrategia = new EstrategiaEnrutamiento(config);

        this.threadPool = Executors.newFixedThreadPool(10); // puedes ajustar el tamaño
    }

    public void verificarConectividadInicial() {
        System.out.println("=== VERIFICANDO CONECTIVIDAD CON DESTINOS ===");
        for (ConfiguracionBroker.Destino destino : config.getDestinosActivos()) {
            boolean conectado = verificador.verificarConectividad(destino);
            if (conectado) {

                // Mostrar informacion detallada del destino conectado
                System.out.println("[OK] Conexion exitosa con " + destino.getId() + 
                                 " (" + destino.getTipo() + ") en " + 
                                 destino.getHost() + ":" + destino.getPuerto());

                System.out.println("[OK] Conexion exitosa con " + destino.getId() +
                        " (" + destino.getTipo() + ") en " +
                        destino.getHost() + ":" + destino.getPuerto());

            } else {
                System.out.println("[!] ADVERTENCIA: No se pudo conectar con " + destino.getId() +
                        " (" + destino.getTipo() + ") en " +
                        destino.getHost() + ":" + destino.getPuerto());
            }
        }
        System.out.println("=== VERIFICACION DE CONECTIVIDAD COMPLETADA ===");
    }

    public boolean procesarVoto(Voto voto) {

        System.out.println(voto.getCandidato().getNombre());
        // Solo reenviar el voto
        return comunicacion.reenviarVoto(voto);
    }    /**
     * Valida un voto reenviando la solicitud al destino correspondiente.
     * Por ahora retorna 1 temporalmente, pero el flujo esta preparado para
     * recibir códigos 0-3 desde el servidor central.
     * 
     * @param documento Documento del votante como String
     * @param candidatoId ID del candidato elegido
     * @return Código de validación: 0=puede votar, 1=no es su mesa, 2=ya votó, 3=no existe
     */

        threadPool.submit(() -> comunicacion.reenviarVoto(voto));
        return true;
    }

    public int validarVoto(String documento, Integer candidatoId) {
        Future<Integer> future = threadPool.submit(() ->
                comunicacion.reenviarValidacionVotante(documento, candidatoId)
        );
        try {
            return future.get();
        } catch (Exception e) {
            return 3;
        }
    }

    public String consultarLugar(String cedula) {
        Future<String> future = threadPool.submit(() -> {
            System.out.println("[THREAD] Ejecutando consulta en: " + Thread.currentThread().getName());
            return comunicacion.reenviarConsultaLugar(cedula);
        });
        try {
            return future.get();
        } catch (Exception e) {
            return "ERROR: No se pudo realizar la consulta.";
        }
    }

    public boolean verificarDestinoAntesDEnvio(ConfiguracionBroker.Destino destino) {
        return verificador.verificarConectividad(destino);
    }

    public void verificarEstadoDestinos() {
        verificador.verificarConectividadDestinos(config);
    }


    public void cerrar() {
        threadPool.shutdown();
        if (verificador != null) {
            verificador.cerrar();
        }
        if (estrategia != null) {
            estrategia.cerrar();
        }
        if (comunicacion != null) {
            comunicacion.cerrarConexion();
        }
    }
}
