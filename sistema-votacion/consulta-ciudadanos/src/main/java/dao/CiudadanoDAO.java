package dao;

import VotingSystem.ConsultaLugarResponse;

import java.sql.*;

public class CiudadanoDAO {

    public ConsultaLugarResponse consultarLugarPorCedula(String cedula) {
        ConsultaLugarResponse response = new ConsultaLugarResponse();
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/elecciones", "postgres", "12345")) {
            String sql = "SELECT c.nombre, c.apellido, d.nombre AS departamento, m.nombre AS municipio, " +
                         "p.nombre AS lugar, p.direccion, c.mesa_id " +
                         "FROM ciudadano c " +
                         "JOIN mesa_votacion mv ON mv.id = c.mesa_id " +
                         "JOIN puesto_votacion p ON p.id = mv.puesto_id " +
                         "JOIN municipio m ON m.id = p.municipio_id " +
                         "JOIN departamento d ON d.id = m.departamento_id " +
                         "WHERE c.documento = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, cedula);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                response.departamento = rs.getString("departamento");
                response.ciudad = rs.getString("municipio");
                response.lugarNombre = rs.getString("lugar");
                response.direccion = rs.getString("direccion");
                response.mesaId = rs.getString("mesa_id");
                String nombre = rs.getString("nombre");
                String apellido = rs.getString("apellido");
                response.encontrado = true;
                response.mensaje = nombre + " " + apellido;;
            } else {
                response.encontrado = false;
                response.mensaje = "No se encontró información";
            }

        } catch (Exception e) {
            response.encontrado = false;
            response.mensaje = "Error en base de datos: " + e.getMessage();
        }
        return response;
    }
}
