package dao;

import model.Extra;

import java.sql.*;

public class ExtraDAO {
    private final Connection con;

    public ExtraDAO(Connection con) {
        this.con = con;
    }
    public void insertar(Extra ex) throws SQLException {
        // primero miro si el extra ya esta en la BD para no meterlo repetido
        String buscar = "SELECT id_extra FROM extra WHERE nombre = ?";
        try (PreparedStatement ps = con.prepareStatement(buscar)) {
            ps.setString(1, ex.getNombre());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ex.setIdExtra(rs.getInt(1));   // ya existia, me quedo con su id
                    return;                        // y salgo sin hacer el insert
                }
            }
        }

        // si llego aqui es que no existe, lo inserto y cojo el id que genera la BD
        String sql = "INSERT INTO extra (nombre) VALUES (?)";
        try(PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1,ex.getNombre());
            ps.executeUpdate();
            try(ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    ex.setIdExtra(rs.getInt(1));
                    return;
                }
            }
        }
    }
}
