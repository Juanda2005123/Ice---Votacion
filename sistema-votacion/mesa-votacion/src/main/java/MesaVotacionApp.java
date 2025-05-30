
import java.util.Scanner;

public class MesaVotacionApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean continuar = true;
        
        System.out.println("=== MESA DE VOTACIÓN ===");
        System.out.println("Sistema iniciado correctamente\n");
        
        while (continuar) {
            System.out.println("1. Registrar voto");
            System.out.println("2. Salir");
            System.out.print("Seleccione una opcion: ");
            
            int opcion = scanner.nextInt();
            
            switch (opcion) {
                case 1:
                    System.out.println("Registrando voto...");
                    // Aquí irá la lógica de votación
                    break;
                case 2:
                    System.out.println("Saliendo del sistema...");
                    continuar = false;
                    break;
                default:
                    System.out.println("Opción inválida");
            }
            System.out.println();
        }
        
        scanner.close();
    }
}