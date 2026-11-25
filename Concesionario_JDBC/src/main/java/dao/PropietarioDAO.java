package dao;

import model.Propietario;

import java.sql.*;

/**
 * DAO de la tabla {@code propietarios}: aquí van todas las consultas SQL de propietarios.
 * <p>
 * Los métodos lanzan {@link SQLException} hacia arriba para que sea el menú quien decida
 * qué mensaje mostrar (por ejemplo, si el DNI ya existe).
 */
public class PropietarioDAO {
    private final Connection con;

    /**
     * @param con conexión abierta a la BD (MySQL o SQLite)
     */
    public PropietarioDAO(Connection con) {
        this.con = con;
    }

    /**
     * Inserta un propietario nuevo y le guarda el id que ha generado la BD.
     *
     * @param p propietario a insertar (sin id)
     * @throws SQLException si falla el INSERT, por ejemplo si el DNI ya existe
     *                      (viola la restricción UNIQUE)
     */
    public void insertar(Propietario p) throws SQLException {
        String sql = "INSERT INTO propietarios (dni, nombre, apellidos, telefono) VALUES (?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getDni());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getApellidos());
            ps.setString(4, p.getTelefono());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setIdPropietario(rs.getInt(1));   // el id que acaba de generar la BD
                }
            }
        }
    }

    /**
     * Busca un propietario por su id.
     *
     * @param id id del propietario
     * @return el propietario, o {@code null} si no existe
     * @throws SQLException si falla la consulta
     */
    public Propietario buscarPorId(int id) throws SQLException {
        String sql = "SELECT id_propietario, dni, nombre, apellidos, telefono FROM propietarios WHERE id_propietario = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return leerUno(ps);
        }
    }

    /**
     * Busca un propietario por su DNI. Se usa en el traspaso para comprobar que el
     * comprador existe.
     *
     * @param dni DNI del propietario
     * @return el propietario, o {@code null} si no existe
     * @throws SQLException si falla la consulta
     */
    public Propietario buscarPorDni(String dni) throws SQLException {
        String sql = "SELECT id_propietario, dni, nombre, apellidos, telefono FROM propietarios WHERE dni = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dni);
            return leerUno(ps);
        }
    }

    /**
     * Método auxiliar: ejecuta el SELECT ya preparado y convierte la primera fila en un
     * {@link Propietario}. Así no repito el mismo código en buscarPorId y buscarPorDni.
     *
     * @param ps consulta preparada con sus parámetros ya puestos
     * @return el propietario, o {@code null} si no hay filas
     * @throws SQLException si falla la consulta
     */
    private Propietario leerUno(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new Propietario(
                        rs.getInt("id_propietario"),
                        rs.getString("dni"),
                        rs.getString("nombre"),
                        rs.getString("apellidos"),
                        rs.getString("telefono"));
            }
        }
        return null;
    }
}
