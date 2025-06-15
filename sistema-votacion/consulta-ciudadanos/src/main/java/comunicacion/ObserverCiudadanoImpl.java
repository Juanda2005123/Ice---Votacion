package comunicacion;

import VotingSystem.ObserverCiudadano;
import VotingSystem.ConsultaLugarResponse;
import com.zeroc.Ice.Current;

public class ObserverCiudadanoImpl implements ObserverCiudadano {
    @Override
    public ConsultaLugarResponse notificarConsulta(String cedula, Current current) {
        System.out.println("[NOTIF] Consulta registrada para cédula: " + cedula);

        // Simulación de respuesta vacía
        ConsultaLugarResponse response = new ConsultaLugarResponse();
        response.encontrado = false;
        response.mensaje = "Información no disponible temporalmente.";
        return response;
    }
}
