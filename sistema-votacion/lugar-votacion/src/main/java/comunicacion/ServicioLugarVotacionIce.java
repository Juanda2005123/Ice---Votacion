package comunicacion;

import VotingSystem.ConsultaLugarResponse;
import VotingSystem.LugarVotacionService;
import VotingSystem.Votante;
import VotingSystem.VotingServicePrx;
import com.zeroc.Ice.Current;

public class ServicioLugarVotacionIce implements LugarVotacionService {

    private String lugarId;

    public ServicioLugarVotacionIce(String lugarId) {
        this.lugarId = lugarId;
    }

    @Override
    public ConsultaLugarResponse consultarLugarVotacion(String cedula, Current current) {
        ConsultaLugarResponse response = new ConsultaLugarResponse();
        try {
            // En un sistema real: buscar en DB o cache
            Votante votante = new Votante(cedula, "Nombre", "Apellido", "DepartamentoX", "CiudadY", false);

            // Lógica para decidir el servidor central (puede ser balanceado)
            VotingServicePrx servidor = VotingServicePrx.checkedCast(
                current.adapter.getCommunicator().stringToProxy("ServidorCentral:tcp -h localhost -p 10000")
            );

            int factores = contarFactoresPrimos(Long.parseLong(cedula));
            boolean esPrimo = esPrimo(factores);

            // Simulación: solo llenamos campos de respuesta
            response.departamento = votante.departamento;
            response.ciudad = votante.ciudad;
            response.lugarNombre = "Colegio Nacional";
            response.direccion = "Calle 123";
            response.mesaId = "MESA-001";
            response.encontrado = true;
            response.mensaje = esPrimo ? "Es primo" : "No es primo";

        } catch (Exception e) {
            response.encontrado = false;
            response.mensaje = "Error en la consulta: " + e.getMessage();
        }

        return response;
    }

    private int contarFactoresPrimos(long n) {
        int count = 0;
        for (long i = 2; i <= n; i++) {
            while (n % i == 0) {
                count++;
                n /= i;
            }
        }
        return count;
    }

    private boolean esPrimo(int numero) {
        if (numero < 2) return false;
        for (int i = 2; i <= Math.sqrt(numero); i++) {
            if (numero % i == 0) return false;
        }
        return true;
    }
}
