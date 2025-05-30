package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilidades para manejo de fechas en el sistema de votación.
 */
public class DateUtil {
    
    // Formateadores predefinidos
    public static final DateTimeFormatter FORMATO_COMPLETO = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    public static final DateTimeFormatter FORMATO_SOLO_FECHA = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    public static final DateTimeFormatter FORMATO_SOLO_HORA = 
        DateTimeFormatter.ofPattern("HH:mm:ss");
    
    /**
     * Formatea una fecha y hora en formato completo (dd/MM/yyyy HH:mm:ss)
     */
    public static String formatearCompleto(LocalDateTime fechaHora) {
        return fechaHora.format(FORMATO_COMPLETO);
    }
    
    /**
     * Formatea solo la fecha (dd/MM/yyyy)
     */
    public static String formatearFecha(LocalDateTime fechaHora) {
        return fechaHora.format(FORMATO_SOLO_FECHA);
    }
    
    /**
     * Formatea solo la hora (HH:mm:ss)
     */
    public static String formatearHora(LocalDateTime fechaHora) {
        return fechaHora.format(FORMATO_SOLO_HORA);
    }
    
    /**
     * Obtiene la fecha y hora actual formateada
     */
    public static String obtenerAhoraFormateada() {
        return formatearCompleto(LocalDateTime.now());
    }
    
    /**
     * Genera un ID único basado en timestamp
     */
    public static String generarIdVoto() {
        return "VOTO_" + System.currentTimeMillis();
    }
}
