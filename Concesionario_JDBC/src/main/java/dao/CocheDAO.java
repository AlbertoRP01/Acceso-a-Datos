package dao;

import model.Coche;
import model.Extra;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de la tabla {@code coches} (y de la tabla intermedia {@code coche_extra}).
 * <p>
 * Los métodos lanzan {@link SQLException} hacia arriba para que sea el menú quien decida
 * qué mensaje mostrar (por ejemplo, si la matrícula ya existe).
 */
public class CocheDAO {
    /**
     * SELECT base de un coche, con el DNI de su propietario. Así no repito la lista en cada
     * consulta. Es LEFT JOIN para que salgan también los coches sin propietario
     * (con dni = NULL).
     */
    private static final String SELECT_COCHE =
            "SELECT c.matricula, c.marca, c.modelo, c.precio, c.id_propietario, p.dni"
                    + " FROM coches c LEFT JOIN propietarios p ON c.id_propietario = p.id_propietario";

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
            // setObject admite null (setInt no): si es del concesionario se guarda NULL
            ps.setObject(5, co.getIdPropietario(), Types.INTEGER);
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

    /**
     * Busca un coche por su matrícula, con sus extras. Se usa para comprobar que el coche
     * existe antes de modificarlo, borrarlo o traspasarlo.
     *
     * @param matricula matrícula del coche
     * @return el coche, o {@code null} si no existe
     * @throws SQLException si falla la consulta
     */
    public Coche buscarPorMatricula(String matricula) throws SQLException {
        String sql = SELECT_COCHE + " WHERE c.matricula = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, matricula);
            List<Coche> lista = leerLista(ps);
            return lista.isEmpty() ? null : lista.get(0);
        }
    }

    /**
     * Opción 6: coches que son del concesionario (sin propietario).
     *
     * @return lista de coches (vacía si no hay ninguno)
     * @throws SQLException si falla la consulta
     */
    public List<Coche> listarSinPropietario() throws SQLException {
        // IS NULL y no "= NULL": en SQL, comparar con = NULL nunca da verdadero
        String sql = SELECT_COCHE + " WHERE c.id_propietario IS NULL ORDER BY c.matricula";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            return leerLista(ps);
        }
    }

    /**
     * Opción 7: coches que tienen propietario, con el DNI del propietario (JOIN).
     *
     * @return lista de coches con {@code dniPropietario} relleno (vacía si no hay ninguno)
     * @throws SQLException si falla la consulta
     */
    public List<Coche> listarConPropietario() throws SQLException {
        // el JOIN solo devuelve los coches cuyo id_propietario coincide con un propietario,
        // así que los que tienen NULL (del concesionario) quedan fuera solos
        String sql = "SELECT c.matricula, c.marca, c.modelo, c.precio, c.id_propietario, p.dni"
                + " FROM coches c JOIN propietarios p ON c.id_propietario = p.id_propietario"
                + " ORDER BY p.dni, c.matricula";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            return leerLista(ps);
        }
    }

    /**
     * Opción 11 (solo MySQL): llama al procedimiento almacenado {@code sp_coches_por_marca}.
     * <p>
     * Se usa {@link CallableStatement}, que es el que sirve para llamar a procedimientos.
     * Como CallableStatement hereda de PreparedStatement, puedo leer el resultado con el
     * mismo método auxiliar {@code leerLista} que el resto de consultas.
     *
     * @param marca marca a buscar
     * @return coches de esa marca (vacía si no hay ninguno)
     * @throws SQLException si falla la llamada (por ejemplo, si el procedimiento no existe)
     */
    public List<Coche> listarPorMarcaProcedimiento(String marca) throws SQLException {
        // {call ...} es la sintaxis estándar de JDBC para llamar a un procedimiento
        try (CallableStatement cs = con.prepareCall("{call sp_coches_por_marca(?)}")) {
            cs.setString(1, marca);
            return leerLista(cs);
        }
    }

    /**
     * Opción 8: actualiza los datos de un coche, menos la matrícula (que es la clave
     * primaria y sirve para saber qué coche cambiar).
     *
     * @param co coche con los datos nuevos
     * @return número de filas actualizadas: 1 si se ha actualizado, 0 si no existe esa matrícula
     * @throws SQLException si falla el UPDATE
     */
    public int actualizar(Coche co) throws SQLException {
        String sql = "UPDATE coches SET marca = ?, modelo = ?, precio = ? WHERE matricula = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, co.getMarca());
            ps.setString(2, co.getModelo());
            ps.setBigDecimal(3, co.getPrecio());
            ps.setString(4, co.getMatricula());
            // executeUpdate devuelve cuántas filas ha cambiado: 0 = esa matrícula no existe
            return ps.executeUpdate();
        }
    }

    /**
     * Opción 9: borra un coche. Sus extras (coche_extra) y sus traspasos se borran solos
     * por el {@code ON DELETE CASCADE} de las tablas.
     *
     * @param matricula matrícula del coche a borrar
     * @return número de filas borradas: 1 si se ha borrado, 0 si no existe esa matrícula
     * @throws SQLException si falla el DELETE
     */
    public int borrar(String matricula) throws SQLException {
        String sql = "DELETE FROM coches WHERE matricula = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, matricula);
            return ps.executeUpdate();
        }
    }

    /**
     * Opción 10 (traspaso): el comprador pasa a ser el dueño del coche. No hace commit:
     * la transacción la controla el service, junto con el INSERT en traspasos.
     *
     * @param matricula     matrícula del coche
     * @param idPropietario id del nuevo propietario (el comprador)
     * @throws SQLException si falla el UPDATE
     */
    public void cambiarPropietario(String matricula, int idPropietario) throws SQLException {
        String sql = "UPDATE coches SET id_propietario = ? WHERE matricula = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPropietario);
            ps.setString(2, matricula);
            ps.executeUpdate();
        }
    }

    /**
     * Método auxiliar: ejecuta el SELECT ya preparado y convierte cada fila en un
     * {@link Coche} con sus extras. Lo usan buscarPorMatricula y los dos listados (todas
     * las consultas traen las mismas columnas, incluido el dni).
     *
     * @param ps consulta preparada con sus parámetros ya puestos
     * @return lista de coches (vacía si no hay filas)
     * @throws SQLException si falla la consulta
     */
    private List<Coche> leerLista(PreparedStatement ps) throws SQLException {
        List<Coche> lista = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                // getInt devuelve 0 si la columna es NULL, por eso compruebo con getObject
                Integer idPropietario = rs.getObject("id_propietario") == null ? null : rs.getInt("id_propietario");
                Coche co = new Coche(
                        rs.getString("matricula"),
                        rs.getString("marca"),
                        rs.getString("modelo"),
                        rs.getBigDecimal("precio"),
                        idPropietario);
                co.setDniPropietario(rs.getString("dni"));   // null si es del concesionario
                lista.add(co);
            }
        }
        // los extras los cargo después de cerrar el ResultSet, para no tener dos abiertos a la vez
        for (Coche co : lista) {
            cargarExtras(co);
        }
        return lista;
    }

    /**
     * Método auxiliar: lee los extras de un coche (JOIN entre extra y coche_extra) y se
     * los añade.
     *
     * @param co coche al que añadir sus extras
     * @throws SQLException si falla la consulta
     */
    private void cargarExtras(Coche co) throws SQLException {
        String sql = "SELECT e.id_extra, e.nombre FROM extra e"
                + " JOIN coche_extra ce ON e.id_extra = ce.id_extra"
                + " WHERE ce.matricula = ? ORDER BY e.nombre";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, co.getMatricula());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    co.addExtra(new Extra(rs.getInt("id_extra"), rs.getString("nombre")));
                }
            }
        }
    }
}
