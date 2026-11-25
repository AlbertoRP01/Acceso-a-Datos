package service;

import exceptions.AppException;
import utils.PropertiesReader;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Genera el informe resumen {@code informe_concesionario.txt} (opción 12 del menú) con:
 * <ul>
 *     <li>el número total de coches ({@code COUNT(*)}),</li>
 *     <li>el listado de coches agrupados por marca ({@code GROUP BY}),</li>
 *     <li>el extra que más se repite.</li>
 * </ul>
 * Primero se hacen las consultas y se van guardando las líneas en una lista; al final se
 * escribe el fichero de una vez con {@code Files.write} (NIO.2, igual que el CSV se lee
 * con {@code Files.readAllLines}).
 */
public class InformeService {
    private static final String SEPARADOR = "==================================================";

    /**
     * Genera el informe y lo escribe en la ruta de {@code informe.ruta}. Si el fichero ya
     * existe, lo sobrescribe.
     *
     * @param con conexión abierta
     * @return la ruta completa del fichero generado, para mostrársela al usuario
     * @throws AppException si falla alguna consulta o no se puede escribir el fichero
     */
    public static Path generar(Connection con) throws AppException {
        Path ruta = PropertiesReader.getRuta("informe.ruta");
        List<String> lineas = new ArrayList<>();

        lineas.add(SEPARADOR);
        lineas.add("  INFORME DEL CONCESIONARIO");
        lineas.add("  Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        lineas.add(SEPARADOR);

        try {
            lineas.add("");
            lineas.add("Número total de coches: " + totalCoches(con));

            lineas.add("");
            lineas.add("COCHES AGRUPADOS POR MARCA");
            lineas.add("--------------------------");
            lineas.addAll(cochesPorMarca(con));

            lineas.add("");
            lineas.add("EXTRA MÁS REPETIDO");
            lineas.add("------------------");
            lineas.addAll(extraMasRepetido(con));

            // Files.write no crea carpetas: si la ruta tiene carpeta (informes/) la creo antes.
            // createDirectories no da error si ya existe
            if (ruta.getParent() != null) {
                Files.createDirectories(ruta.getParent());
            }
            // Files.write crea el fichero (o lo sobrescribe), escribe todas las líneas y lo cierra
            Files.write(ruta, lineas, StandardCharsets.UTF_8);
            return ruta.toAbsolutePath();

        } catch (SQLException e) {
            throw new AppException("Error al consultar los datos del informe: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AppException("No se ha podido escribir el informe en " + ruta.toAbsolutePath()
                    + ": " + e.getMessage(), e);
        }
    }

    /**
     * Número total de coches con {@code COUNT(*)}.
     *
     * @param con conexión abierta
     * @return número de coches
     * @throws SQLException si falla la consulta
     */
    private static int totalCoches(Connection con) throws SQLException {
        String sql = "SELECT COUNT(*) FROM coches";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();               // COUNT(*) siempre devuelve una fila (0 si no hay coches)
            return rs.getInt(1);
        }
    }

    /**
     * Listado de coches agrupados por marca. Primero un {@code GROUP BY} saca cada marca
     * con cuántos coches tiene, y después, para cada marca, se listan sus coches.
     *
     * @param con conexión abierta
     * @return las líneas de esta sección del informe
     * @throws SQLException si falla alguna consulta
     */
    private static List<String> cochesPorMarca(Connection con) throws SQLException {
        List<String> lineas = new ArrayList<>();

        // 1. GROUP BY: cada marca con su número de coches. Lo guardo en un mapa
        //    (LinkedHashMap mantiene el orden del ORDER BY) y cierro el ResultSet,
        //    para no tener dos consultas abiertas a la vez en la misma conexión
        Map<String, Integer> marcas = new LinkedHashMap<>();
        String sqlMarcas = "SELECT marca, COUNT(*) AS total FROM coches GROUP BY marca ORDER BY marca";
        try (PreparedStatement ps = con.prepareStatement(sqlMarcas);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                marcas.put(rs.getString("marca"), rs.getInt("total"));
            }
        }

        // 2. Para cada marca, sus coches. El mismo PreparedStatement sirve para todas:
        //    solo cambia el parámetro
        String sqlCoches = "SELECT matricula, modelo, precio FROM coches WHERE marca = ? ORDER BY modelo, matricula";
        try (PreparedStatement ps = con.prepareStatement(sqlCoches)) {
            for (Map.Entry<String, Integer> m : marcas.entrySet()) {
                lineas.add(m.getKey() + " (" + m.getValue() + ")");
                ps.setString(1, m.getKey());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        BigDecimal precio = rs.getBigDecimal("precio");
                        lineas.add("   - " + rs.getString("matricula") + "  "
                                + rs.getString("modelo") + "  " + precio + " €");
                    }
                }
            }
        }

        if (lineas.isEmpty()) {
            lineas.add("No hay coches");
        }
        return lineas;
    }

    /**
     * El extra (equipamiento) que más se repite entre todos los coches. Si hay empate,
     * salen todos los empatados (por eso no se usa LIMIT 1, que se quedaría con uno al azar).
     *
     * @param con conexión abierta
     * @return las líneas de esta sección del informe
     * @throws SQLException si falla la consulta
     */
    private static List<String> extraMasRepetido(Connection con) throws SQLException {
        List<String> lineas = new ArrayList<>();
        // HAVING se queda con los extras cuyo número de coches es igual al máximo.
        // La subconsulta calcula ese máximo: cuenta los coches de cada extra y coge el mayor
        String sql = "SELECT e.nombre, COUNT(*) AS veces"
                + " FROM coche_extra ce JOIN extra e ON ce.id_extra = e.id_extra"
                + " GROUP BY e.id_extra, e.nombre"
                + " HAVING COUNT(*) = (SELECT MAX(n) FROM"
                + "     (SELECT COUNT(*) AS n FROM coche_extra GROUP BY id_extra) AS conteo)"
                + " ORDER BY e.nombre";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lineas.add(rs.getString("nombre") + " (en " + rs.getInt("veces") + " coches)");
            }
        }
        if (lineas.isEmpty()) {
            lineas.add("Ningún coche tiene extras");
        }
        return lineas;
    }
}
