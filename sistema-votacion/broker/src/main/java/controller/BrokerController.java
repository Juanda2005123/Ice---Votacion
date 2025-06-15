package controller;

import model.Voto;
import config.ConfiguracionBroker;
import comunicacion.ServicioComunicacionBroker;
import comunicacion.ServicioVerificacionConectividad;

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
        this.threadPool = Executors.newFixedThreadPool(10); // puedes ajustar el tamaño
    }

    public void verificarConectividadInicial() {
        System.out.println("=== VERIFICANDO CONECTIVIDAD CON DESTINOS ===");
        for (ConfiguracionBroker.Destino destino : config.getDestinosActivos()) {
            boolean conectado = verificador.verificarConectividad(destino);
            if (conectado) {
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
        if (comunicacion != null) {
            comunicacion.cerrarConexion();
        }
        System.out.println("BrokerController cerrado");
    }
}
