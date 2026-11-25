package dao;

import model.Coche;
import model.Extra;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * DAO de la tabla {@code coches} (y de la tabla intermedia {@code coche_extra}).
 * <p>
 * Los métodos lanzan {@link SQLException} hacia arriba para que sea el menú quien decida
 * qué mensaje mostrar (por ejemplo, si la matrícula ya existe).
 */
public class CocheDAO {
    private final Connection con;
    private final ExtraDAO extraDAO;

    /**
     * @param con conexión abierta a la BD (MySQL o SQLite)
     */
    public CocheDAO(Connection con) {
        this.con = con;
        this.extraDAO = new ExtraDAO(con);
    }

    /**
     * Inserta un coche y sus extras. Cada extra se inserta en {@code extra} (si no
     * existía ya) y se relaciona con el coche en {@code coche_extra}.
     *
     * @param co coche a insertar
     * @throws SQLException si falla el INSERT, por ejemplo si la matrícula ya existe
     *                      (viola la clave primaria)
     */
    public void insertar(Coche co) throws SQLException {
        String sql = "INSERT INTO coches (matricula, marca, modelo, precio, id_propietario) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, co.getMatricula());
            ps.setString(2, co.getMarca());
            ps.setString(3, co.getModelo());
            ps.setBigDecimal(4, co.getPrecio());
            // si no tiene propietario (es del concesionario) hay que meter NULL con setNull,
            // porque setInt no admite null
            if (co.tienePropietario()) {
                ps.setInt(5, co.getIdPropietario());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.executeUpdate();
        }

        String sqlRel = "INSERT INTO coche_extra (matricula, id_extra) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sqlRel)) {
            for (Extra e : co.getExtras()) {
                extraDAO.insertar(e);   // le pone el id al extra (nuevo o el que ya existía)
                ps.setString(1, co.getMatricula());
                ps.setInt(2, e.getIdExtra());
                ps.executeUpdate();
            }
        }
    }
}
