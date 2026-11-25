package utils;

import exceptions.AppException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gestiona la única conexión a la base de datos del programa (MySQL o SQLite).
 * <p>
 * Todo es {@code static} porque solo hay una conexión abierta a la vez: se abre en la
 * opción 1 del menú, la usan todos los DAO y se cierra al salir del programa.
 * Los datos de conexión se leen de {@code config.properties}.
 */
public class ConexionDB {

    /** Gestores de base de datos que soporta el programa. */
    public enum Motor { MYSQL, SQLITE }

    private static Connection conexion;   // la conexión abierta (null si no hay)
    private static Motor motorActual;     // a cuál estoy conectado (null si no hay)

    /**
     * Abre la conexión con el motor elegido. Si ya había una abierta, la cierra antes
     * (por si el usuario cambia de MySQL a SQLite sin salir del programa).
     *
     * @param motor MYSQL o SQLITE
     * @return la conexión abierta
     * @throws AppException si no se puede leer la configuración o no se puede conectar
     */
    public static Connection abrir(Motor motor) throws AppException {
        cerrar();
        PropertiesReader props = PropertiesReader.getInstance();
        try {
            // switch con flechas: cada caso llama a su método auxiliar y devuelve la conexión
            conexion = switch (motor) {
                case MYSQL -> abrirMysql(props);
                case SQLITE -> abrirSqlite(props);
            };
            motorActual = motor;
            return conexion;
        } catch (SQLException e) {
            throw new AppException("No se pudo conectar a " + motor + ": " + e.getMessage(), e);
        }
    }

    /**
     * MySQL: me conecto al servidor, creo la base de datos si no existe y me quedo
     * trabajando dentro de ella.
     *
     * @param props configuración leída de {@code config.properties}
     * @return la conexión abierta
     * @throws SQLException si el servidor no responde o el usuario/contraseña no son válidos
     */
    private static Connection abrirMysql(PropertiesReader props) throws SQLException {
        Connection con = DriverManager.getConnection(
                props.get("mysql.url"),
                props.get("mysql.user"),
                props.get("mysql.password"));

        String bd = props.get("mysql.database");
        // try-with-resources en el Statement: se cierra solo. La conexión NO, sigue abierta
        try (Statement st = con.createStatement()) {
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + bd);
        }
        con.setCatalog(bd);   // equivale a hacer "USE concesionario"
        return con;
    }

    /**
     * SQLite: el fichero {@code .db} se crea solo si no existe. Hay que activar las claves
     * foráneas, porque en SQLite vienen desactivadas por defecto.
     *
     * @param props configuración leída de {@code config.properties}
     * @return la conexión abierta
     * @throws SQLException si no se puede abrir o crear el fichero
     */
    private static Connection abrirSqlite(PropertiesReader props) throws SQLException {
        Connection con = DriverManager.getConnection(props.get("sqlite.url"));
        try (Statement st = con.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return con;
    }

    /**
     * Devuelve la conexión abierta, para pasársela a los DAO.
     *
     * @return la conexión abierta
     * @throws AppException si todavía no se ha conectado (opción 1 del menú)
     */
    public static Connection getConexion() throws AppException {
        if (conexion == null) {
            throw new AppException("Primero conecta con una base de datos (opción 1)");
        }
        return conexion;
    }

    /**
     * @return el motor al que estoy conectado, o {@code null} si no hay conexión
     */
    public static Motor getMotor() {
        return motorActual;
    }

    /**
     * Cierra la conexión si está abierta. Si falla al cerrar solo avisa, y deja la conexión
     * a {@code null} igualmente para poder volver a conectar.
     */
    public static void cerrar() {
        if (conexion != null) {
            try {
                conexion.close();
            } catch (SQLException e) {
                System.out.println("Aviso: no se pudo cerrar la conexión: " + e.getMessage());
            }
            conexion = null;
            motorActual = null;
        }
    }
}
