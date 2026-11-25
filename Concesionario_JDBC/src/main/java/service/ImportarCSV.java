package service;

import dao.CocheDAO;
import exceptions.AppException;
import model.Coche;
import model.Extra;
import utils.PropertiesReader;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Importa coches desde el fichero CSV (opción 5 del menú).
 * <p>
 * Se hace en dos pasos:
 * <ol>
 *     <li>{@link #leerCSV()}: lee el fichero y lo convierte en una lista de coches. Si el CSV
 *     está mal escrito falla aquí, sin haber tocado la BD.</li>
 *     <li>{@link #importar(Connection)}: inserta todos los coches en una única transacción:
 *     o se insertan todos, o ninguno.</li>
 * </ol>
 * Formato del CSV: {@code matricula;marca;modelo;extras;precio}, con los extras separados
 * por {@code |}. La primera línea es la cabecera.
 */
public class ImportarCSV {
    private final Path rutaCsv;

    /**
     * Lee la ruta del CSV de {@code config.properties} (clave {@code csv.ruta}). Si es
     * relativa, se busca desde la carpeta en la que se ejecuta el programa (la del proyecto).
     *
     * @throws AppException si falta la clave en el fichero de configuración
     */
    public ImportarCSV() throws AppException {
        this.rutaCsv = Path.of(leerRuta("csv.ruta"));
    }

    /**
     * Método auxiliar: lee una ruta del fichero de configuración y comprueba que esté.
     *
     * @param clave clave en {@code config.properties}
     * @return la ruta, sin espacios
     * @throws AppException si la clave no está o está vacía
     */
    private static String leerRuta(String clave) throws AppException {
        String ruta = PropertiesReader.getInstance().get(clave);
        if (ruta == null || ruta.isBlank()) {
            throw new AppException("Falta la ruta '" + clave + "' en el fichero config.properties. Ábrelo y añádela.");
        }
        return ruta.trim();
    }

    /**
     * Paso 1: lee el CSV del disco y devuelve los coches que hay en él. No toca la BD:
     * las matrículas repetidas las detecta luego la BD al insertar, porque es la clave
     * primaria.
     *
     * @return la lista de coches del CSV
     * @throws AppException si el CSV no existe, no se puede leer o alguna línea está mal
     */
    public List<Coche> leerCSV() throws AppException {
        List<Coche> coches = new ArrayList<>();
        int numLinea = 0;   // para decirle al usuario en qué línea está el fallo

        try {
            // readAllLines abre el fichero, lee todas las líneas y lo cierra solo
            // (por eso no hace falta try-with-resources)
            List<String> lineas = Files.readAllLines(rutaCsv, StandardCharsets.UTF_8);

            // empiezo en 1 para saltarme la cabecera (matricula;marca;modelo;extras;precio)
            for (int i = 1; i < lineas.size(); i++) {
                numLinea = i + 1;   // i + 1 porque el usuario cuenta las líneas desde 1
                // las líneas vacías (por ejemplo al final del fichero) me las salto
                if (!lineas.get(i).isBlank()) {
                    coches.add(convertirLinea(lineas.get(i)));
                }
            }
        } catch (NoSuchFileException e) {
            // va antes que IOException porque es hija suya
            throw new AppException("No encuentro el fichero CSV. Debería estar en: " + rutaCsv.toAbsolutePath(), e);
        } catch (IOException e) {
            throw new AppException("No se ha podido leer el fichero CSV: " + e.getMessage(), e);
        } catch (NumberFormatException e) {
            throw new AppException("La línea " + numLinea + " del CSV tiene un precio no válido", e);
        } catch (IllegalArgumentException e) {
            // la lanza convertirLinea si a la línea le faltan columnas
            throw new AppException("La línea " + numLinea + " del CSV está mal: " + e.getMessage(), e);
        }
        return coches;
    }

    /**
     * Paso 2: inserta en la BD todos los coches del CSV en una única transacción.
     * Si falla uno (por ejemplo, una matrícula repetida) se hace rollback y no se queda
     * ninguno insertado.
     *
     * @param con conexión abierta
     * @return número de coches importados
     * @throws AppException si el CSV está mal o falla algún INSERT (y no se habrá importado ninguno)
     */
    public int importar(Connection con) throws AppException {
        List<Coche> coches = leerCSV();   // si el CSV está mal, falla aquí sin tocar la BD
        CocheDAO dao = new CocheDAO(con);
        String matricula = "";            // para decir en qué coche ha fallado

        try {
            con.setAutoCommit(false);     // a partir de aquí nada se guarda hasta el commit
            for (Coche co : coches) {
                matricula = co.getMatricula();
                dao.insertar(co);
            }
            con.commit();                 // todo bien: se guardan todos de golpe
            return coches.size();

        } catch (SQLException e) {
            deshacer(con);                // algo falló: se deshacen TODOS los inserts
            throw new AppException("Error al importar el coche " + matricula
                    + ". No se ha importado ninguno: " + e.getMessage(), e);
        } finally {
            // dejo la conexión como estaba; si no, las demás opciones del menú no se guardarían
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Aviso: no se pudo restaurar el autocommit: " + e.getMessage());
            }
        }
    }

    /**
     * Método auxiliar: convierte una línea del CSV en un {@link Coche} con sus extras.
     *
     * @param linea línea del CSV, por ejemplo {@code 3456JKL;Seat;Ibiza;Climatizador|Sensor;17999.99}
     * @return el coche
     * @throws IllegalArgumentException si a la línea le faltan columnas
     * @throws NumberFormatException    si el precio no es un número
     */
    private Coche convertirLinea(String linea) {
        String[] campos = linea.split(";");
        if (campos.length < 5) {
            throw new IllegalArgumentException("tiene " + campos.length + " columnas y deben ser 5");
        }

        // campos[0] matrícula, [1] marca, [2] modelo, [3] extras, [4] precio
        Coche co = new Coche(
                campos[0].trim(),
                campos[1].trim(),
                campos[2].trim(),
                new BigDecimal(campos[4].trim()));

        // | es un carácter especial en split (expresión regular), por eso va escapado
        for (String extra : campos[3].split("\\|")) {
            // un coche sin extras tiene " " en esa columna: los trozos vacíos los ignoro
            if (!extra.isBlank()) {
                co.addExtra(new Extra(extra.trim()));
            }
        }
        return co;
    }

    /**
     * Método auxiliar: hace rollback sin que un fallo aquí tape el error original.
     *
     * @param con conexión en la que deshacer los cambios
     */
    private static void deshacer(Connection con) {
        try {
            con.rollback();
        } catch (SQLException e) {
            System.out.println("Aviso: no se pudo hacer rollback: " + e.getMessage());
        }
    }
}
