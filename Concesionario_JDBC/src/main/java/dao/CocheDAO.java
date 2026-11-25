package dao;

import model.Coche;
import model.Extra;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class CocheDAO {
    private final Connection con;
    private final ExtraDAO extraDAO;

    public CocheDAO(Connection con) {
        this.con = con;
        this.extraDAO = new ExtraDAO(con);
    }

    public void insertar(Coche co) throws SQLException {
        String sql = "INSERT INTO coche (matricula, marca, modelo, precio) VALUES (?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, co.getMatricula());
            ps.setString(2, co.getMarca());
            ps.setString(3, co.getModelo());
            ps.setBigDecimal(4, co.getPrecio());
            ps.executeUpdate();
        }
        String sqlRel = "INSERT INTO coche_extra (matricula, id_extra) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sqlRel)) {
            for (Extra e : co.getExtras()) {
                extraDAO.insertar(e);
                ps.setString(1, co.getMatricula());
                ps.setInt(2, e.getIdExtra());
                ps.executeUpdate();
            }
        }
    }
}
