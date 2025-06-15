import controller.ControllerVoto;
import java.util.Scanner;

public class ConsultaVotosApp {
    public static void main(String[] args) {
        ControllerVoto controller = new ControllerVoto();
        controller.iniciar();

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("1. Consultar lugar de votación");
            System.out.println("2. Salir");
            System.out.print("Seleccione una opción: ");
            String opcion = sc.nextLine();

            if (opcion.equals("1")) {
                System.out.print("Ingrese la cédula del votante: ");
                String cedula = sc.nextLine();
                controller.consultarLugarPorCedula(cedula);
            } else if (opcion.equals("2")) {
                break;
            } else {
                System.out.println("[ERROR] Opcion invalida. Intente nuevamente.");
            }
        }

        controller.cerrar();
        System.out.println("[INFO] Aplicación finalizada.");
    }
}