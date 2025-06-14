package dao;

import VotingSystem.ConsultaLugarResponse;
import java.sql.*;

public class CiudadanoDAO {

    private Connection conn;

    public CiudadanoDAO() throws Exception {
        conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/elecciones", "postgres", "12345");
    }

    public ConsultaLugarResponse obtenerLugarPorCedula(String cedula) throws Exception {
        ConsultaLugarResponse resp = new ConsultaLugarResponse();
        String sql = 
            "SELECT m.id AS mesa_id, p.nombre AS lugar, p.direccion, mu.nombre AS municipio, d.nombre AS departamento " +
            "FROM ciudadano c " +
            "JOIN mesa_votacion m ON c.mesa_id = m.id " +
            "JOIN puesto_votacion p ON m.puesto_id = p.id " +
            "JOIN municipio mu ON p.municipio_id = mu.id " +
            "JOIN departamento d ON mu.departamento_id = d.id " +
            "WHERE c.documento = ?";


        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cedula);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                resp.mesaId = "MESA-" + rs.getInt("mesa_id");
                resp.lugarNombre = rs.getString("lugar");
                resp.direccion = rs.getString("direccion");
                resp.ciudad = rs.getString("municipio");
                resp.departamento = rs.getString("departamento");
                resp.encontrado = true;
                resp.mensaje = "Consulta exitosa";
            } else {
                resp.encontrado = false;
                resp.mensaje = "No se encontró el ciudadano";
            }
        }

        return resp;
    }
}
