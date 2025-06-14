package dao;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class CiudadanoDAO {
    private Connection connection;

    public CiudadanoDAO(Connection connection) {
        this.connection = connection;
    }

    public Map<String, String> consultarLugarPorCedula(String cedula) throws SQLException {
        String query = "SELECT d.nombre AS departamento, m.nombre AS municipio, " +
                       "p.nombre AS puesto, p.direccion, mv.consecutive AS mesa " +
                       "FROM ciudadano c " +
                       "JOIN mesa_votacion mv ON c.mesa_id = mv.id " +
                       "JOIN puesto_votacion p ON mv.puesto_id = p.id " +
                       "JOIN municipio m ON p.municipio_id = m.id " +
                       "JOIN departamento d ON m.departamento_id = d.id " +
                       "WHERE c.documento = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, cedula);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, String> result = new HashMap<>();
                    result.put("departamento", rs.getString("departamento"));
                    result.put("municipio", rs.getString("municipio"));
                    result.put("puesto", rs.getString("puesto"));
                    result.put("direccion", rs.getString("direccion"));
                    result.put("mesa", rs.getString("mesa"));
                    return result;
                }
            }
        }

        return null;
    }
}
