package controller;

import model.Voto;
import precarga.SistemaPrecarga;
import model.Candidato;
import model.Ciudadano;
import ui.VotacionUI;
import votos.RepositorioMesaVotacion;
import votos.VerificacionVoto;
import comunicacion.ServicioComunicacionIce;
import deltas.GeneradorDeltas;
import deltas.RepositorioDeltas;
import VotingSystem.DeltaConteo;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Controlador principal de votacion para gestion de mesa de votacion.
 * Implementa logica de negocio y coordina entre capas de UI y datos.
 * 
 * Este controlador maneja:
 * - Validacion de elegibilidad de votantes usando busquedas rapidas HashMap (complejidad O(1))
 * - Procesamiento y registro de votos
 * - Comunicacion con servidor central via Ice
 * - Manejo comprehensivo de errores y retroalimentacion al usuario
 * 
 * Arquitectura:
 * - Sigue patron MVC separando UI, logica de negocio y datos
 * - Usa Cadena de Responsabilidad para pasos de validacion
 * - Disenado para escenarios de votacion de alto rendimiento
 * 
 * @author Sistema de Votacion
 * @version 1.0
 * @since 2025-05-30
 */
public class ControllerVotacion {    // ===== VARIABLES DE INSTANCIA =====
    private VotacionUI ui;                              // Manejador de interfaz de usuario
    private String idMesaVotacion;                      // Identificador unico para esta mesa de votacion
    private RepositorioMesaVotacion repositorio;        // Repositorio centralizado de datos de votacion
    private int contadorSecuencialVotos;                // Contador secuencial para generar IDs de votos
    private config.ConfiguracionMesaVotacion configProperties; // Configuracion cargada desde archivo .properties
    private VerificacionVoto verificacionVoto;          // Encargado de validar votos antes de enviar
    
    // ===== VARIABLES PARA SISTEMA DE DELTAS =====
    private GeneradorDeltas generadorDeltas;            // Generador de deltas Map-Reduce
    private RepositorioDeltas repositorioDeltas;        // Repositorio de conteos absolutos para auditoría
    private ExecutorService threadPoolDeltas;           // Pool de hilos para procesamiento de deltas
    private ServicioComunicacionIce servicioIce;        // Servicio Ice para comunicación
    
    // ===== CONSTRUCTOR =====
    /**
     * Constructor para ControllerVotacion.
     * Inicializa UI, estructuras de datos de votacion y carga votantes elegibles.
     * 
     * @param idMesaVotacion Identificador unico para esta mesa de votacion
     */      
    public ControllerVotacion() {
        this.ui = new VotacionUI();
        this.contadorSecuencialVotos = 1;  // Inicializar contador en 1
        
        // Cargar configuracion desde archivo .properties
        this.configProperties = cargarConfiguracionArchivo();
        
        // Usar el ID de mesa del archivo de configuracion en lugar del parametro
        this.idMesaVotacion = configProperties.getMesaId();
          // Inicializar repositorio centralizado
        this.repositorio = new RepositorioMesaVotacion(this.idMesaVotacion);
        this.servicioIce = new ServicioComunicacionIce();
        this.verificacionVoto = new VerificacionVoto(repositorio, servicioIce);

        // ===== INICIALIZAR SISTEMA DE DELTAS =====
        // Inicializar repositorio de deltas para conteos absolutos y auditoría
        this.repositorioDeltas = new RepositorioDeltas(this.idMesaVotacion);
        
        // Inicializar generador de deltas para Map-Reduce
        this.generadorDeltas = new GeneradorDeltas(configProperties);
          // Crear pool de hilos configurado para procesamiento de deltas
        this.threadPoolDeltas = Executors.newFixedThreadPool(configProperties.getThreadsPoolSize());        // Cargar datos iniciales con candidatos desde CSV
        String rutaCandidatos = "candidatos.csv"; // Archivo junto al JAR
        SistemaPrecarga sistemaPrecarga = new SistemaPrecarga(repositorio, rutaCandidatos);
        // Realizar precarga de datos
        realizarPrecarga(sistemaPrecarga);
    }
    
    // ===== METODOS DE INICIALIZACION =====
      /**
     * Realiza la precarga de datos para la mesa de votacion.
     * Carga candidatos desde CSV y genera votantes simulados.
     */
    @SuppressWarnings("IncompleteIceConnection")
    private void realizarPrecarga(SistemaPrecarga sistemaPrecarga) {
        try {
            // Precargar datos desde CSV y simulados
            sistemaPrecarga.precargarMesa();
            
            ui.mostrarMensajeInfo("Mesa precargada exitosamente:");
            ui.mostrarMensajeInfo("- Candidatos: " + repositorio.getCandidatosDisponibles().size());
            ui.mostrarMensajeInfo("- Votantes elegibles: " + repositorio.getTotalVotantesElegibles());
              } catch (Exception e) {
            ui.mostrarMensajeError("Error durante la precarga: " + e.getMessage());
            throw new RuntimeException("No se pudo precargar la mesa de votacion");
        }
    }
    
    /**
     * Carga la configuracion desde el archivo .properties externo o interno.
     * Busca primero un archivo externo en el directorio actual, luego uno interno.
     * 
     * @return Configuracion cargada desde el archivo .properties
     */    private config.ConfiguracionMesaVotacion cargarConfiguracionArchivo() {
        try {
            // Buscar archivo externo en directorio actual
            java.io.File archivoExterno = new java.io.File("mesa-votacion.properties");
            if (archivoExterno.exists()) {
                System.out.println("Cargando configuracion desde archivo externo: mesa-votacion.properties");
                return new config.ConfiguracionMesaVotacion("mesa-votacion.properties");
            } else {
                // Usar archivo interno del JAR
                System.out.println("Cargando configuracion desde archivo interno (JAR)");
                return new config.ConfiguracionMesaVotacion("src/main/resources/mesa-votacion.properties");
            }
        } catch (Exception e) {
            System.err.println("Error cargando configuracion: " + e.getMessage());
            throw new RuntimeException("No se pudo cargar la configuracion de mesa");
        }
    }
    
    // ===== FLUJO PRINCIPAL DE APLICACION =====
    
    /**
     * Metodo principal que ejecuta el flujo de la aplicacion.
     * Muestra opciones de menu y maneja interacciones del usuario hasta el apagado del sistema.
     */
    public void iniciar() {
        boolean continuarEjecutando = true;
        
        ui.mostrarMensajeInfo("Sistema de votacion iniciado - Mesa de Votacion: " + idMesaVotacion);
        ui.limpiarPantalla();
        
        while (continuarEjecutando) {
            try {
                int opcion = ui.mostrarMenuPrincipal();
                ui.limpiarPantalla();
                
                switch (opcion) {
                    case 1:
                        procesarVoto();
                        break;                    
                    case 2:
                        ui.mostrarMensajeInfo("Cerrando sistema de votacion...");
                        cerrarSistemaConDeltas();
                        continuarEjecutando = false;
                        break;default:
                        ui.mostrarMensajeError("Opcion invalida. Por favor seleccione 1 o 2.");
                        ui.limpiarPantalla();
                }            } catch (Exception e) {
                ui.mostrarMensajeError("Error inesperado: " + e.getMessage());
                ui.limpiarPantalla();
            }        }

        cerrarSistemaConDeltas();
        ui.cerrar();
    }
    
    // ===== METODOS DE VALIDACION =====
    
    /**
     * Valida la elegibilidad del votante para esta mesa de votacion.
     * Verifica si el votante esta registrado y asignado a esta mesa.
     * 
     * @param documento El numero de documento del votante
     * @return Objeto Votante si es elegible, null si no se encuentra
     */
    private Ciudadano validarElegibilidadVotante(String documento) {
        return repositorio.obtenerVotantePorDocumento(documento);
    }
      /**
     * Confirma y procesa el voto con sistema de deltas únicamente.
     * 
     * FLUJO CON DELTAS:
     * 1. Marca al votante como que ya votó
     * 2. Registra voto en repositorio de deltas (conteo absoluto para auditoría)
     * 3. Añade voto al buffer de deltas (conteo incremental)
     * 4. Verifica umbrales y envía delta si es necesario
     * 
     * @param votante El votante que emitio el voto
     * @param voto El voto que fue emitido
     * @throws IllegalArgumentException si hay error de validacion
     * @throws RuntimeException si hay error en el procesamiento
     */    private void confirmarVoto(Ciudadano votante, Voto voto) {
        try {
            // 1. MARCAR VOTANTE COMO QUE YA VOTÓ
            votante.setYaVoto(true);
            
            // 2. REGISTRAR EN REPOSITORIO ORIGINAL (mantener estructura)
            repositorio.registrarVoto(voto);
            
            // 3. REGISTRAR EN REPOSITORIO DE DELTAS (conteo absoluto para auditoría)
            repositorioDeltas.registrarVoto(voto);
            
            // 4. AÑADIR AL BUFFER DE DELTAS (conteo incremental para Map-Reduce)  
            generadorDeltas.registrarVoto(voto.getCandidato());
            
            // 5. VERIFICAR UMBRALES Y PROCESAR DELTAS
            if (generadorDeltas.debeEnviarDelta()) {
                procesarEnvioDeltas();
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Error en procesamiento de voto", e);
        }
    }
    
    // ===== PROCESAMIENTO DE VOTOS =====
    
    /**
     * Procesa el registro de un nuevo voto.
     * Implementa el flujo completo de votacion con validacion y confirmacion.
     * 
     * Flujo de trabajo:
     * 1. Capturar documento del votante
     * 2. Validar elegibilidad del votante
     * 3. Validar estado de votacion
     * 4. Mostrar opciones de candidatos
     * 5. Capturar seleccion de voto
     * 6. Confirmar voto con votante
     * 7. Registrar voto y actualizar estado del votante
     * 8. Enviar a servidor central
     */
    private void procesarVoto() {
        try {
            // 1. Capturar documento
            String documento = ui.capturarDocumento();
            
            // 5. Mostrar candidatos disponibles (desde repositorio)
            ui.mostrarCandidatos(repositorio.getCandidatosDisponibles());
            
            // 6. Capturar seleccion de candidato
            int seleccion = ui.capturarSeleccionCandidato(repositorio.getCandidatosDisponibles().size());
            
            // 7. Obtener candidato seleccionado
            Candidato candidatoSeleccionado = repositorio.getCandidatosDisponibles().get(seleccion - 1);
            int valid = validarVoto(documento, candidatoSeleccionado.getId());
            switch (valid) {
                case 0:
                    // Puede votar - proceder con el registro

                    Ciudadano votante = validarElegibilidadVotante(documento);

                    Integer votoId = generarIdVotoSecuencial();
                    Voto nuevoVoto = new Voto(votoId, candidatoSeleccionado);

                    confirmarVoto(votante, nuevoVoto);
                    
                    // Mostrar confirmacion de exito
                    ui.mostrarMensajeExito("Voto registrado exitosamente.");
                    ui.mostrarMensajeInfo("Votante: " + votante.getNombre() + " " + votante.getApellido());
                    ui.mostrarMensajeInfo("Candidato: " + candidatoSeleccionado.getNombre());
                    break;                case 1:
                    // No es su mesa de votacion
                    ui.mostrarMensajeError("Este documento no corresponde a esta mesa de votacion.");
                    ui.mostrarMensajeInfo("Por favor, dirijase a la mesa de votacion que le corresponde.");
                    break;
                case 2:
                    // Ya voto
                    ui.mostrarMensajeError("Este ciudadano ya ha ejercido su derecho al voto.");
                    ui.mostrarMensajeInfo("Cada ciudadano solo puede votar una vez.");
                    break;case 3: 
                    // No existe en la base de datos
                    ui.mostrarMensajeError("El documento ingresado no se encuentra registrado en el sistema.");
                    ui.mostrarMensajeInfo("Verifique que el numero de documento sea correcto.");
                    ui.mostrarMensajeInfo("Si el problema persiste, consulte con el personal electoral.");
                    break;
                default:
                    // Codigo de error no reconocido
                    ui.mostrarMensajeError("Error inesperado en la validacion del voto (codigo: " + valid + ").");
                    ui.mostrarMensajeInfo("Por favor, consulte con el personal electoral.");
                    break;
            }
            
        } catch (IllegalArgumentException e) {
            ui.mostrarMensajeError(e.getMessage());
        } catch (RuntimeException e) {
            ui.mostrarMensajeError("Error durante el proceso de votacion: " + e.getMessage());
        } catch (Exception e) {
            ui.mostrarMensajeError("Error inesperado durante el proceso de votacion: " + e.getMessage());        }
        
        ui.limpiarPantalla();
    }
    
    private Integer validarVoto(String documento, Integer candidatoId) {
        return verificacionVoto.validarVoto(documento, candidatoId);
    }

    /**
     * Procesa el envío de deltas cuando se alcanzan los umbrales configurados.
     * Ejecuta el envío de forma asíncrona usando el pool de hilos.
     * 
     * FLUJO DE ENVIO DE DELTAS:
     * 1. Genera el objeto DeltaConteo con el buffer actual
     * 2. Envía el delta al broker de forma asíncrona
     * 3. Resetea el buffer después del envío exitoso
     * 4. Maneja errores sin afectar el flujo principal de votación
     */    private void procesarEnvioDeltas() {
        // Usar pool de hilos para no bloquear el flujo principal
        threadPoolDeltas.submit(() -> {
            try {
                // 1. GENERAR DELTA CON BUFFER ACTUAL
                DeltaConteo delta = generadorDeltas.generarDelta();
                
                if (delta == null) {
                    return;
                }
                
                // 2. ENVIAR DELTA AL BROKER
                boolean envioExitoso = servicioIce.enviarDelta(delta);
                
                if (envioExitoso) {
                    // 3. RESETEAR BUFFER DESPUES DE ENVIO EXITOSO
                    generadorDeltas.resetearBuffer();
                } else {
                    // El buffer NO se resetea en caso de error, se reintentará en próximo umbral
                }
                
            } catch (Exception e) {
                // El buffer NO se resetea en caso de excepción
            }
        });
    }

    // ===== METODOS DE UTILIDAD =====
    
    /**
     * Genera un ID unico para el voto combinando el numero de mesa y un contador secuencial.
     * Formato: XXXNN donde XXX es el numero de mesa y NN es el contador secuencial.
     * Ejemplo: Mesa 001 -> primer voto: 00101, segundo voto: 00102, etc.
     * Ejemplo: Mesa 018 -> primer voto: 01801, segundo voto: 01802, etc.
     * 
     * @return ID unico del voto como Integer
     */
    private Integer generarIdVotoSecuencial() {
        // Extraer numero de mesa del ID (formato MESA-XXX)
        String numeroMesa = idMesaVotacion.substring(idMesaVotacion.lastIndexOf('-') + 1);
        
        // Formatear ID: numero de mesa (3 digitos) + contador secuencial (2 digitos)
        String idVoto = String.format("%s%02d", numeroMesa, contadorSecuencialVotos);
        
        // Incrementar contador para el proximo voto
        contadorSecuencialVotos++;
        
        // Convertir a Integer y retornar
        return Integer.valueOf(idVoto);
    }
    
    // ===== METODOS GETTER (Para pruebas y depuracion) =====
    
    /**
     * Obtiene el numero total de votos registrados.
     * 
     * @return Total de votos emitidos en esta mesa de votacion
     */
    public int getTotalVotosRegistrados() {
        return repositorio.getTotalVotos();
    }
    
    /**
     * Verifica si un votante ya ha votado.
     * 
     * @param documento Numero de documento del votante
     * @return true si el votante ya voto, false en caso contrario
     */
    public boolean yaVotoElVotante(String documento) {
        Ciudadano votante = repositorio.obtenerVotantePorDocumento(documento);
        return votante != null && votante.isYaVoto();
    }
    
    /**
     * Verifica si un votante es elegible para esta mesa de votacion.
     * 
     * @param documento Numero de documento del votante
     * @return true si el votante es elegible, false en caso contrario
     */
    public boolean esVotanteElegible(String documento) {
        return repositorio.esVotanteElegible(documento);
    }
    
    /**
     * Obtiene un votante por su numero de documento.
     * 
     * @param documento Numero de documento del votante
     * @return Objeto Votante o null si no se encuentra
     */
    public Ciudadano obtenerVotante(String documento) {
        return repositorio.obtenerVotantePorDocumento(documento);
    }
    
    /**
     * Obtiene el numero total de votantes elegibles.
     * 
     * @return Total de votantes elegibles para esta mesa de votacion
     */
    public int getTotalVotantesElegibles() {
        return repositorio.getTotalVotantesElegibles();
    }
    
    /**
     * Obtiene el numero total de votantes que ya han votado.
     * 
     * @return Conteo de votantes que han emitido sus votos
     */
    public int getTotalVotantesQueYaVotaron() {
        return repositorio.getTotalVotantesQueYaVotaron();
    }
    
    /**
     * Obtiene una copia de la lista de candidatos disponibles.
     * 
     * @return Lista de candidatos disponibles
     */
    public List<Candidato> getCandidatosDisponibles() {
        return repositorio.getCandidatosDisponibles();
    }
    
    /**
     * Obtiene el ID de la mesa de votacion.
     * 
     * @return Identificador unico de esta mesa de votacion
     */
    public String getIdMesaVotacion() {
        return idMesaVotacion;
    }
      /**
     * Cierra el sistema de votacion de forma ordenada, incluyendo el sistema de deltas.
     * 
     * PROCESO DE CIERRE:
     * 1. Envia deltas pendientes si los hay
     * 2. Detiene el coordinador de envio original  
     * 3. Apaga el pool de hilos de deltas de forma ordenada
     * 4. Genera archivo CSV con resultados parciales
     * 5. Muestra estadisticas finales del sistema
     */
    private void cerrarSistemaConDeltas() {
        try {
            ui.mostrarMensajeInfo("Iniciando cierre ordenado del sistema...");
            
            // 1. ENVIAR DELTAS PENDIENTES
            if (generadorDeltas.hayVotosPendientes()) {
                ui.mostrarMensajeInfo("Enviando deltas pendientes...");
                procesarEnvioDeltas();
                
                // Esperar un momento para que se complete el envío
                Thread.sleep(2000);
            }
              // 2. CERRAR SERVICIO ICE
            servicioIce.cerrarConexion();
              // 3. APAGAR POOL DE HILOS DE DELTAS
            threadPoolDeltas.shutdown();
            
            // Esperar a que terminen las tareas en curso (maximo 5 segundos)
            if (!threadPoolDeltas.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                threadPoolDeltas.shutdownNow();
            }
            
            // 4. GENERAR ARCHIVO CSV CON RESULTADOS PARCIALES
            generarArchivoCSVResultados();
            
            // 5. MOSTRAR ESTADISTICAS FINALES
            mostrarEstadisticasFinales();
            
            ui.mostrarMensajeExito("Sistema cerrado exitosamente.");
            
        } catch (Exception e) {
            ui.mostrarMensajeError("Error durante el cierre: " + e.getMessage());
            // Forzar cierre en caso de error
            threadPoolDeltas.shutdownNow();
        }
    }
      /**
     * Muestra estadisticas finales del sistema de votacion y deltas.
     */
    private void mostrarEstadisticasFinales() {
        ui.mostrarMensajeInfo("\n=== ESTADISTICAS FINALES ===");
        ui.mostrarMensajeInfo("Mesa: " + idMesaVotacion);
        ui.mostrarMensajeInfo("Total votos registrados: " + repositorio.getTotalVotos());
        ui.mostrarMensajeInfo("Total votos absolutos (deltas): " + repositorioDeltas.getTotalVotos());
        ui.mostrarMensajeInfo("Votantes que votaron: " + repositorio.getTotalVotantesQueYaVotaron());
        ui.mostrarMensajeInfo("Votantes elegibles: " + repositorio.getTotalVotantesElegibles());
        
        // Mostrar conteo por candidato desde repositorio de deltas
        Map<Integer, Integer> conteoFinal = repositorioDeltas.getConteoAbsoluto();
        if (!conteoFinal.isEmpty()) {
            ui.mostrarMensajeInfo("\nConteo final por candidato:");
            for (Map.Entry<Integer, Integer> entry : conteoFinal.entrySet()) {
                ui.mostrarMensajeInfo("  Candidato " + entry.getKey() + ": " + entry.getValue() + " votos");
            }
        }
        
        ui.mostrarMensajeInfo("\nArchivo CSV generado: partial-" + idMesaVotacion + ".csv");
        ui.mostrarMensajeInfo("==============================\n");
    }
    
    // ===== METODOS GETTER PARA SISTEMA DE DELTAS =====
    
    /**
     * Obtiene estadisticas del buffer de deltas actual.
     * 
     * @return String con informacion del estado del buffer de deltas
     */
    public String getEstadisticasDeltas() {
        return generadorDeltas.getEstadisticasBuffer();
    }
    
    /**
     * Obtiene el conteo absoluto de votos por candidato.
     * 
     * @return Mapa con candidatoId -> total de votos absolutos
     */
    public Map<Integer, Integer> getConteoAbsolutoVotos() {
        return repositorioDeltas.getConteoAbsoluto();
    }
    
    /**
     * Obtiene el total de votos absolutos registrados en el repositorio de deltas.
     * 
     * @return Total de votos absolutos para auditoria
     */
    public int getTotalVotosAbsolutos() {
        return repositorioDeltas.getTotalVotos();
    }
    
    /**
     * Verifica si hay votos pendientes de envío en el buffer de deltas.
     * 
     * @return true si hay votos pendientes en buffer
     */
    public boolean hayDeltasPendientes() {
        return generadorDeltas.hayVotosPendientes();
    }
    
    /**
     * Fuerza el envío de deltas pendientes (para testing o admin).
     * Util para pruebas o cuando se quiere forzar el envio antes del umbral.
     */
    public void forzarEnvioDeltas() {
        if (generadorDeltas.hayVotosPendientes()) {
            procesarEnvioDeltas();
        }
    }
      /**
     * Genera un archivo CSV con los resultados parciales de la mesa de votacion.
     * 
     * El archivo se guarda como: partial-{mesaId}.csv
     * Formato: candidatoId,nombreCandidato,numeroVotos
     * 
     * IMPORTANTE: 
     * - El archivo se genera solo al cerrar la aplicacion
     * - Se sobreescribe si ya existe (no se acumula)
     * - Se guarda en el directorio donde se ejecuta el JAR
     * - Usa los conteos absolutos del repositorio de deltas
     * - Incluye nombres de candidatos desde datos cargados
     */
    private void generarArchivoCSVResultados() {
        String nombreArchivo = "partial-" + idMesaVotacion + ".csv";
        
        try {
            // Obtener conteos absolutos del repositorio de deltas
            Map<Integer, Integer> conteoFinal = repositorioDeltas.getConteoAbsoluto();
            
            // Obtener candidatos disponibles para nombres
            List<Candidato> candidatos = repositorio.getCandidatosDisponibles();
            Map<Integer, String> nombresCandidatos = new HashMap<>();
            for (Candidato candidato : candidatos) {
                nombresCandidatos.put(candidato.getId(), candidato.getNombre());
            }
            
            // Crear el archivo CSV
            try (PrintWriter writer = new PrintWriter(new FileWriter(nombreArchivo))) {
                // Escribir encabezado
                writer.println("candidatoId,nombreCandidato,numeroVotos");
                
                // Escribir datos de cada candidato
                for (Map.Entry<Integer, Integer> entry : conteoFinal.entrySet()) {
                    int candidatoId = entry.getKey();
                    int numeroVotos = entry.getValue();
                    String nombreCandidato = nombresCandidatos.getOrDefault(candidatoId, "Candidato-" + candidatoId);
                    
                    writer.println(candidatoId + "," + nombreCandidato + "," + numeroVotos);
                }
            }
            
        } catch (IOException e) {
            // Error silencioso - no interrumpir cierre
        } catch (Exception e) {
            // Error silencioso - no interrumpir cierre  
        }
    }
    
    /**
     * Genera manualmente el archivo CSV de resultados.
     * Util para testing o para generar resultados intermedios.
     * 
     * @return true si el archivo se genero exitosamente, false en caso contrario
     */
    public boolean generarCSVManualmente() {
        try {
            generarArchivoCSVResultados();
            return true;
        } catch (Exception e) {
            System.err.println("Error generando CSV manualmente: " + e.getMessage());
            return false;
        }
    }
}