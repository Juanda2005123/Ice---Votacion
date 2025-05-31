
import java.util.*;
import model.Voto;

public class ServidorCentralApp {
    // Lista para almacenar votos recibidos (temporal, hasta implementar Ice)
    private static List<Voto> votos = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);
      public static void main(String[] args) {
        System.out.println("=== SERVIDOR CENTRAL DE VOTACIÓN ===");
        System.out.println("Servidor iniciado correctamente");
        System.out.println("Esperando conexiones de mesas de votación...\n");
        
        boolean continuar = true;
        
        while (continuar) {
            mostrarMenu();
            int opcion = scanner.nextInt();
            
            switch (opcion) {
                case 1:
                    verTotalVotos();
                    break;
                case 2:
                    verVotosPorCandidato();
                    break;
                case 3:
                    System.out.println("Cerrando servidor...");
                    continuar = false;
                    break;
                default:
                    System.out.println("Opción inválida");
            }
            System.out.println();
        }
        
        scanner.close();
    }
      private static void mostrarMenu() {
        System.out.println("=== MENÚ SERVIDOR CENTRAL ===");
        System.out.println("1. Ver total de votos");
        System.out.println("2. Ver votos por candidato");
        System.out.println("3. Salir");
        System.out.print("Seleccione una opcion: ");
    }
      private static void verTotalVotos() {
        System.out.println("\n=== TOTAL DE VOTOS ===");
        System.out.println("Total de votos registrados: " + votos.size());
    }
    
    private static void verVotosPorCandidato() {
        System.out.println("\n=== VOTOS POR CANDIDATO ===");
        
        if (votos.isEmpty()) {
            System.out.println("No hay votos registrados");
            return;
        }
        
        Map<String, Integer> conteo = new HashMap<>();
          // Contar votos por candidato
        for (Voto voto : votos) {
            String candidatoNombre = voto.getCandidato().getNombreCompleto();
            conteo.put(candidatoNombre, 
                      conteo.getOrDefault(candidatoNombre, 0) + 1);
        }
        
        // Mostrar resultados
        for (Map.Entry<String, Integer> entry : conteo.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue() + " votos");
        }
    }
}