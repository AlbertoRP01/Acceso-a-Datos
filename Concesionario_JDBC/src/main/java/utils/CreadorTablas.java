package utils;

import exceptions.AppException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Crea la estructura de la base de datos (opción 2 del menú) ejecutando el DDL desde Java.
 * <p>
 * Usa {@code CREATE TABLE IF NOT EXISTS}, así que se puede ejecutar varias veces sin error:
 * si la tabla ya existe, no hace nada y no borra los datos.
 * <p>
 * El mismo SQL vale para MySQL y SQLite. Lo único que cambia es cómo se escribe la clave
 * primaria autonumérica, y eso lo resuelve el método auxiliar {@link #idAutonumerico}.
 */
public class CreadorTablas {

    /**
     * Crea todas las tablas en la base de datos a la que se está conectado.
     * <p>
     * El orden importa: primero las tablas a las que apuntan las claves foráneas
     * ({@code propietarios}, {@code extra}) y después las que las usan.
     *
     * @throws AppException si no hay conexión o falla algún CREATE TABLE
     */
    public static void crearTablas() throws AppException {
        Connection con = ConexionDB.getConexion();
        String id = idAutonumerico(ConexionDB.getMotor());

        String[] ddl = {
                // propietarios: el dni es UNIQUE para que no se pueda repetir
                "CREATE TABLE IF NOT EXISTS propietarios ("
                        + " id_propietario " + id + ","
                        + " dni VARCHAR(9) NOT NULL UNIQUE,"
                        + " nombre VARCHAR(50) NOT NULL,"
                        + " apellidos VARCHAR(100) NOT NULL,"
                        + " telefono VARCHAR(15)"
                        + ")",

                // coches: la matrícula es la clave primaria. id_propietario admite NULL
                // (el coche es del concesionario). Si se borra el propietario, el coche
                // vuelve a ser del concesionario (SET NULL)
                "CREATE TABLE IF NOT EXISTS coches ("
                        + " matricula VARCHAR(10) PRIMARY KEY,"
                        + " marca VARCHAR(50) NOT NULL,"
                        + " modelo VARCHAR(50) NOT NULL,"
                        + " precio DECIMAL(10,2) NOT NULL,"
                        + " id_propietario INT NULL,"
                        + " FOREIGN KEY (id_propietario) REFERENCES propietarios(id_propietario)"
                        + "   ON DELETE SET NULL"
                        + ")",

                // extra: catálogo de equipamientos, el nombre no se repite
                "CREATE TABLE IF NOT EXISTS extra ("
                        + " id_extra " + id + ","
                        + " nombre VARCHAR(50) NOT NULL UNIQUE"
                        + ")",

                // coche_extra: tabla intermedia N:M entre coches y extra. La clave primaria
                // es la pareja, para que un coche no tenga el mismo extra dos veces.
                // ON DELETE CASCADE: si se borra el coche, se borran sus extras
                "CREATE TABLE IF NOT EXISTS coche_extra ("
                        + " matricula VARCHAR(10) NOT NULL,"
                        + " id_extra INT NOT NULL,"
                        + " PRIMARY KEY (matricula, id_extra),"
                        + " FOREIGN KEY (matricula) REFERENCES coches(matricula) ON DELETE CASCADE,"
                        + " FOREIGN KEY (id_extra) REFERENCES extra(id_extra)"
                        + ")",

                // traspasos: id_vendedor admite NULL (vende el concesionario).
                // ON DELETE CASCADE en la matrícula para que la opción 9 (borrar coche)
                // funcione aunque el coche tenga traspasos
                "CREATE TABLE IF NOT EXISTS traspasos ("
                        + " id_traspaso " + id + ","
                        + " matricula VARCHAR(10) NOT NULL,"
                        + " id_vendedor INT NULL,"
                        + " id_comprador INT NOT NULL,"
                        + " monto DECIMAL(10,2) NOT NULL,"
                        + " fecha DATE NOT NULL,"
                        + " FOREIGN KEY (matricula) REFERENCES coches(matricula) ON DELETE CASCADE,"
                        + " FOREIGN KEY (id_vendedor) REFERENCES propietarios(id_propietario),"
                        + " FOREIGN KEY (id_comprador) REFERENCES propietarios(id_propietario)"
                        + ")"
        };

        // un solo Statement para todas las sentencias, y se cierra solo al acabar
        try (Statement st = con.createStatement()) {
            for (String sql : ddl) {
                st.executeUpdate(sql);
            }
            // SQLite no tiene procedimientos almacenados: solo se crea en MySQL
            if (ConexionDB.getMotor() == ConexionDB.Motor.MYSQL) {
                crearProcedimiento(st);
            }
        } catch (SQLException e) {
            throw new AppException("Error al crear las tablas: " + e.getMessage(), e);
        }
    }

    /**
     * Crea el procedimiento almacenado {@code sp_coches_por_marca} (solo MySQL), que
     * devuelve los coches de una marca con el DNI de su propietario.
     * <p>
     * Primero lo borra si existe y luego lo crea, así siempre queda la última versión.
     * Desde JDBC no hace falta el {@code DELIMITER} de Workbench: cada sentencia se
     * envía por separado, así que el {@code ;} de dentro del procedimiento no molesta.
     * <p>
     * Devuelve las mismas columnas que los SELECT de {@code CocheDAO}, para poder leer
     * el resultado con el mismo método auxiliar.
     *
     * @param st Statement abierto sobre la conexión
     * @throws SQLException si falla el DROP o el CREATE
     */
    private static void crearProcedimiento(Statement st) throws SQLException {
        st.executeUpdate("DROP PROCEDURE IF EXISTS sp_coches_por_marca");
        st.executeUpdate(
                "CREATE PROCEDURE sp_coches_por_marca(IN p_marca VARCHAR(50))"
                        + " BEGIN"
                        + "   SELECT c.matricula, c.marca, c.modelo, c.precio, c.id_propietario, p.dni"
                        + "   FROM coches c LEFT JOIN propietarios p ON c.id_propietario = p.id_propietario"
                        + "   WHERE c.marca = p_marca"
                        + "   ORDER BY c.modelo, c.matricula;"
                        + " END");
    }

    /**
     * Método auxiliar: devuelve cómo se escribe una clave primaria autonumérica en cada
     * gestor, que es lo único del DDL que no es igual en MySQL y en SQLite.
     *
     * @param motor gestor al que se está conectado
     * @return el trozo de SQL para la columna id
     */
    private static String idAutonumerico(ConexionDB.Motor motor) {
        return switch (motor) {
            case MYSQL -> "INT AUTO_INCREMENT PRIMARY KEY";
            case SQLITE -> "INTEGER PRIMARY KEY AUTOINCREMENT";
        };
    }
}
