package dao;

import model.Propietario;

import java.sql.*;

public class PropietarioDAO {
    private final Connection con;

    public PropietarioDAO(Connection con) {
        this.con = con;
    }

    public void insertar(Propietario p) throws SQLException {
        String sql = "INSERT INTO propietario (nombre, apellidos, telefono) VALUES (?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getApellidos());
            ps.setString(3, p.getTelefono());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) p.setIdPropietario(1);
            }
        }
    }

    public Propietario buscarPorId(int id) throws SQLException {
        String sql = "SELECT id_propietario, nombre, apellidos telefono FROM propietario WHERE id_propietario = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Propietario(rs.getInt("id_propietario"),
                            rs.getString("nombre"),
                            rs.getString("apellidos"),
                            rs.getString("telefonos"));
                }
            }
        }
        return  null;
    }
}
