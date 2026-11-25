package dao;

import model.Traspaso;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de la tabla {@code traspasos}: aquí van todas las consultas SQL de traspasos.
 * <p>
 * Este DAO NO hace commit ni rollback: un traspaso también cambia el propietario del coche
 * (tabla {@code coches}), así que la transacción la controla el service, que es el que sabe
 * dónde empieza y dónde acaba la operación completa.
 * <p>
 * Los métodos lanzan {@link SQLException} hacia arriba para que sea quien los llama el que
 * decida qué hacer (rollback, mensaje al usuario...).
 */
public class TraspasoDAO {
    private final Connection con;

    /**
     * @param con conexión abierta a la BD (MySQL o SQLite)
     */
    public TraspasoDAO(Connection con) {
        this.con = con;
    }

    /**
     * Inserta un traspaso y le guarda el id que ha generado la BD.
     *
     * @param t traspaso a insertar (sin id)
     * @throws SQLException si falla el INSERT, por ejemplo si la matrícula o el comprador
     *                      no existen (violan la clave foránea)
     */
    public void insertar(Traspaso t) throws SQLException {
        String sql = "INSERT INTO traspasos (matricula, id_vendedor, id_comprador, monto, fecha) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getMatriculaCoche());
            // setObject admite null (setInt no): si vende el concesionario se guarda NULL
            ps.setObject(2, t.getIdVendedor(), Types.INTEGER);
            ps.setInt(3, t.getIdComprador());
            ps.setBigDecimal(4, t.getMontoEconomico());
            // la fecha la guardo como texto 'AAAA-MM-DD': MySQL lo acepta en una columna DATE
            // y SQLite (que no tiene tipo fecha) la guarda tal cual, así funciona en las dos
            ps.setString(5, t.getFecha().toString());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    t.setIdTraspaso(rs.getInt(1));   // el id que acaba de generar la BD
                }
            }
        }
    }

    /**
     * Devuelve todos los traspasos, del más antiguo al más reciente.
     *
     * @return lista de traspasos (vacía si no hay ninguno)
     * @throws SQLException si falla la consulta
     */
    public List<Traspaso> listarTodos() throws SQLException {
        String sql = "SELECT id_traspaso, matricula, id_vendedor, id_comprador, monto, fecha FROM traspasos ORDER BY fecha, id_traspaso";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            return leerLista(ps);
        }
    }

    /**
     * Devuelve el historial de traspasos de un coche, del más antiguo al más reciente.
     *
     * @param matricula matrícula del coche
     * @return lista de traspasos del coche (vacía si nunca se ha traspasado)
     * @throws SQLException si falla la consulta
     */
    public List<Traspaso> listarPorMatricula(String matricula) throws SQLException {
        String sql = "SELECT id_traspaso, matricula, id_vendedor, id_comprador, monto, fecha FROM traspasos WHERE matricula = ? ORDER BY fecha, id_traspaso";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, matricula);
            return leerLista(ps);
        }
    }

    /**
     * Método auxiliar: ejecuta el SELECT ya preparado y convierte cada fila en un
     * {@link Traspaso}. Así no repito el mismo código en listarTodos y listarPorMatricula.
     *
     * @param ps consulta preparada con sus parámetros ya puestos
     * @return lista de traspasos (vacía si no hay filas)
     * @throws SQLException si falla la consulta
     */
    private List<Traspaso> leerLista(PreparedStatement ps) throws SQLException {
        List<Traspaso> lista = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                // getInt devuelve 0 si la columna es NULL, por eso uso getObject para el vendedor
                Integer idVendedor = rs.getObject("id_vendedor") == null ? null : rs.getInt("id_vendedor");
                lista.add(new Traspaso(
                        rs.getInt("id_traspaso"),
                        rs.getString("matricula"),
                        idVendedor,
                        rs.getInt("id_comprador"),
                        rs.getBigDecimal("monto"),
                        LocalDate.parse(rs.getString("fecha"))));
            }
        }
        return lista;
    }
}
