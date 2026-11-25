import dao.CocheDAO;
import dao.PropietarioDAO;
import exceptions.AppException;
import model.Coche;
import model.Extra;
import model.Propietario;
import model.Traspaso;
import service.ImportarCSV;
import service.TraspasoService;
import utils.ConexionDB;
import utils.CreadorTablas;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Scanner;

/**
 * Programa principal: menú de texto del concesionario.
 * <p>
 * El Main solo pide datos por consola, llama a los DAO / services y muestra los resultados.
 * Los errores se recogen en un único sitio ({@link #main}) para no repetir try/catch en
 * cada opción.
 */
public class Main {
    private static final Scanner sc = new Scanner(System.in);

    /**
     * Bucle del menú: muestra las opciones, ejecuta la elegida y gestiona sus errores.
     *
     * @param args no se usan
     */
    public static void main(String[] args) {
        String opcion;
        do {
            mostrarMenu();
            opcion = sc.nextLine().trim();
            try {
                ejecutarOpcion(opcion);
            } catch (AppException e) {
                System.out.println("❌ " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("❌ " + mensajeSQL(e));
            } catch (NumberFormatException e) {
                System.out.println("❌ Número no válido. Usa el formato 15000.50");
            }
        } while (!opcion.equals("0"));

        ConexionDB.cerrar();
        sc.close();
        System.out.println("¡Hasta luego!");
    }

    /** Muestra las opciones del menú. */
    private static void mostrarMenu() {
        System.out.println();
        System.out.println("===== CONCESIONARIO =====" + (ConexionDB.getMotor() != null ? " [" + ConexionDB.getMotor() + "]" : ""));
        System.out.println(" 1. Conectar BBDD");
        System.out.println(" 2. Inicializar / crear tablas");
        System.out.println(" 3. Registrar propietario");
        System.out.println(" 4. Insertar coche");
        System.out.println(" 5. Importar coches (CSV)");
        System.out.println(" 6. Listar coches (concesionario)");
        System.out.println(" 7. Listar coches (propietarios)");
        System.out.println(" 8. Modificar coche");
        System.out.println(" 9. Borrar coche");
        System.out.println("10. Realizar traspaso");
        System.out.println("11. Ejecutar procedimiento almacenado (solo MySQL)");
        System.out.println("12. Generar informe resumen");
        System.out.println(" 0. Salir");
        System.out.print("Opción: ");
    }

    /**
     * Ejecuta la opción elegida. Las excepciones suben al {@link #main}, que es quien
     * muestra el mensaje.
     *
     * @param opcion texto que ha escrito el usuario
     * @throws AppException          si no hay conexión u otro error de la aplicación
     * @throws SQLException          si falla una consulta
     * @throws NumberFormatException si el usuario escribe mal un número
     */
    private static void ejecutarOpcion(String opcion) throws AppException, SQLException {
        switch (opcion) {
            case "1" -> conectar();
            case "2" -> {
                CreadorTablas.crearTablas();
                System.out.println("✅ Tablas creadas (o ya existían)");
            }
            case "3" -> registrarPropietario();
            case "4" -> insertarCoche();
            case "5" -> {
                int total = new ImportarCSV().importar(ConexionDB.getConexion());
                System.out.println("✅ Importados " + total + " coches del CSV");
            }
            case "6" -> mostrarCoches(new CocheDAO(ConexionDB.getConexion()).listarSinPropietario());
            case "7" -> mostrarCoches(new CocheDAO(ConexionDB.getConexion()).listarConPropietario());
            case "8" -> modificarCoche();
            case "9" -> borrarCoche();
            case "10" -> realizarTraspaso();
            case "11" -> cochesPorMarca();
            case "12" -> System.out.println("⏳ Pendiente: falta el service de esta opción");
            case "0" -> { }   // sale del bucle en el main
            default -> System.out.println("Opción no válida");
        }
    }

    // ===================== OPCIONES =====================

    /** Opción 1: el usuario elige MySQL o SQLite y se prueba la conexión. */
    private static void conectar() throws AppException {
        System.out.print("1) MySQL   2) SQLite: ");
        ConexionDB.Motor motor = switch (sc.nextLine().trim()) {
            case "1" -> ConexionDB.Motor.MYSQL;
            case "2" -> ConexionDB.Motor.SQLITE;
            default -> throw new AppException("Opción no válida, elige 1 o 2");
        };
        ConexionDB.abrir(motor);
        System.out.println("✅ Conectado a " + motor);
    }

    /** Opción 3: pide los datos de un propietario y lo inserta. */
    private static void registrarPropietario() throws AppException, SQLException {
        PropietarioDAO dao = new PropietarioDAO(ConexionDB.getConexion());
        Propietario p = new Propietario(
                leerTexto("DNI: "),
                leerTexto("Nombre: "),
                leerTexto("Apellidos: "),
                leerTexto("Teléfono: "));
        dao.insertar(p);
        System.out.println("✅ Propietario registrado con id " + p.getIdPropietario());
    }

    /** Opción 4: pide los datos de un coche y lo inserta sin propietario (es del concesionario). */
    private static void insertarCoche() throws AppException, SQLException {
        CocheDAO dao = new CocheDAO(ConexionDB.getConexion());
        Coche co = new Coche(
                leerTexto("Matrícula: "),
                leerTexto("Marca: "),
                leerTexto("Modelo: "),
                leerPrecio("Precio: "));
        String extras = leerTexto("Extras separados por comas (Enter si no tiene): ");
        for (String nombre : extras.split(",")) {
            if (!nombre.isBlank()) {
                co.addExtra(new Extra(nombre.trim()));
            }
        }
        dao.insertar(co);
        System.out.println("✅ Coche insertado");
    }

    /** Opción 8: pide una matrícula y los datos nuevos (menos la matrícula) y actualiza. */
    private static void modificarCoche() throws AppException, SQLException {
        CocheDAO dao = new CocheDAO(ConexionDB.getConexion());
        String matricula = leerTexto("Matrícula del coche a modificar: ");
        Coche actual = dao.buscarPorMatricula(matricula);
        if (actual == null) {
            System.out.println("No existe ningún coche con la matrícula " + matricula);
            return;
        }
        System.out.println(actual);
        Coche nuevo = new Coche(
                matricula,
                leerTexto("Nueva marca: "),
                leerTexto("Nuevo modelo: "),
                leerPrecio("Nuevo precio: "));
        dao.actualizar(nuevo);
        System.out.println("✅ Coche modificado");
    }

    /** Opción 9: pide una matrícula y borra el coche. */
    private static void borrarCoche() throws AppException, SQLException {
        CocheDAO dao = new CocheDAO(ConexionDB.getConexion());
        String matricula = leerTexto("Matrícula del coche a borrar: ");
        if (dao.borrar(matricula) == 0) {
            System.out.println("No existe ningún coche con la matrícula " + matricula);
        } else {
            System.out.println("✅ Coche borrado");
        }
    }

    /** Opción 10: pide DNI del comprador, matrícula e importe, y hace el traspaso. */
    private static void realizarTraspaso() throws AppException {
        Traspaso t = TraspasoService.realizar(
                ConexionDB.getConexion(),
                leerTexto("DNI del comprador: "),
                leerTexto("Matrícula del coche: "),
                leerPrecio("Importe: "));
        System.out.println("✅ Traspaso realizado");
        System.out.println(t);
    }

    /** Opción 11 (solo MySQL): pide una marca y llama al procedimiento almacenado. */
    private static void cochesPorMarca() throws AppException, SQLException {
        // compruebo primero la conexión (getConexion lanza "Primero conecta..." si no hay)
        CocheDAO dao = new CocheDAO(ConexionDB.getConexion());
        if (ConexionDB.getMotor() != ConexionDB.Motor.MYSQL) {
            throw new AppException("Los procedimientos almacenados solo funcionan con MySQL (SQLite no los tiene)");
        }
        mostrarCoches(dao.listarPorMarcaProcedimiento(leerTexto("Marca: ")));
    }

    // ===================== MÉTODOS AUXILIARES =====================

    /**
     * Muestra una lista de coches, o un aviso si está vacía. Lo usan las opciones 6 y 7.
     *
     * @param coches coches a mostrar
     */
    private static void mostrarCoches(List<Coche> coches) {
        if (coches.isEmpty()) {
            System.out.println("No hay coches");
        }
        for (Coche co : coches) {
            System.out.println(co);
        }
    }

    /**
     * Pide un texto por consola y lo devuelve sin espacios al principio ni al final.
     *
     * @param mensaje lo que se le muestra al usuario
     * @return el texto escrito
     */
    private static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return sc.nextLine().trim();
    }

    /**
     * Pide un precio por consola. Admite coma o punto decimal (15000,50 o 15000.50).
     *
     * @param mensaje lo que se le muestra al usuario
     * @return el precio
     * @throws NumberFormatException si no es un número (lo recoge el main)
     */
    private static BigDecimal leerPrecio(String mensaje) {
        return new BigDecimal(leerTexto(mensaje).replace(',', '.'));
    }

    /**
     * Traduce una SQLException a un mensaje para el usuario. Las violaciones de clave
     * (matrícula o DNI repetidos) se detectan de forma distinta en cada gestor:
     * MySQL lanza {@link SQLIntegrityConstraintViolationException} y SQLite usa el
     * código de error 19 (SQLITE_CONSTRAINT).
     *
     * @param e excepción que ha lanzado el DAO
     * @return mensaje para mostrar
     */
    private static String mensajeSQL(SQLException e) {
        if (e instanceof SQLIntegrityConstraintViolationException || e.getErrorCode() == 19) {
            return "Dato repetido o no válido (¿matrícula o DNI ya existentes?): " + e.getMessage();
        }
        return "Error en la base de datos: " + e.getMessage();
    }
}
