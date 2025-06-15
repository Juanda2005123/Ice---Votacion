package model;

import java.time.LocalDateTime;

/**
 * Respuesta que envía el servidor al recibir un voto.
 * Implementa el patrón Reliable Message con ACK inmediato.
 */
public class VotoResponse {
    private Integer id;           // ID único del voto
    private boolean received;    // Confirmación de recepción
    private long timestamp;      // Timestamp del ACK
    private String message;      // Mensaje descriptivo
    
    public VotoResponse(Integer id, boolean received, String message) {
        this.id = id;
        this.received = received;
        this.timestamp = System.currentTimeMillis();
        this.message = message;
    }
    
    // Constructor para respuesta exitosa
    public static VotoResponse success(Integer votoId) {
        return new VotoResponse(votoId, true, "Voto recibido exitosamente");
    }
    
    // Constructor para respuesta de error
    public static VotoResponse error(String errorMessage) {
        return new VotoResponse(null, false, errorMessage);
    }
      // Getters
    public Integer getId() {
        return id;
    }
    
    public boolean isReceived() {
        return received;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getMessage() {
        return message;
    }
    
    @Override
    public String toString() {
        return String.format("VotoResponse{id='%s', received=%s, message='%s', timestamp=%d}", 
                           id, received, message, timestamp);
    }
}
