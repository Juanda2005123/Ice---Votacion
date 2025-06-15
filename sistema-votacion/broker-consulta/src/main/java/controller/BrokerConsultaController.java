package controller;

import config.ConfiguracionBroker;
import comunicacion.ServicioComunicacionConsultaBroker;
import comunicacion.ServicioVerificacionConectividad;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Controlador principal del broker de consultas ciudadanas.
 * Maneja el reenvío de consultas mediante ThreadPool configurable.
 */
public class BrokerConsultaController {

    private final ServicioComunicacionConsultaBroker comunicacion;
    private final ServicioVerificacionConectividad verificador;
    private final ConfiguracionBroker config;
    private final ExecutorService threadPool;

    public BrokerConsultaController(ConfiguracionBroker config) {
        this.config = config;
        this.comunicacion = new ServicioComunicacionConsultaBroker(config);
        this.verificador = new ServicioVerificacionConectividad();
        int hilos = Integer.parseInt(config.getPropiedad("threadpool.size", "10"));
        this.threadPool = Executors.newFixedThreadPool(hilos);
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

    public String consultarLugar(String cedula) {
        Future<String> future = threadPool.submit(() -> comunicacion.reenviarConsultaLugar(cedula));
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
        System.out.println("BrokerConsultaController cerrado");
    }
}