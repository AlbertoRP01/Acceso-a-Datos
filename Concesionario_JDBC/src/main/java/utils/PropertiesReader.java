package utils;


import exceptions.AppException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Lee el fichero {@code config.properties} y da acceso a sus valores (datos de conexión
 * a las BBDD y rutas de ficheros), para no tener nada de eso escrito en el código.
 * <p>
 * Es un Singleton: el fichero se lee una sola vez y todas las clases usan la misma
 * instancia a través de {@link #getInstance()}.
 * <p>
 * El fichero se lee desde el classpath (no desde una ruta del disco): Maven copia lo que
 * hay en {@code src/main/resources} a {@code target/classes}, y ahí es donde se busca.
 */
public class PropertiesReader {
    private static final String FICHERO = "config.properties";
    private static PropertiesReader instance = null;
    private final Properties p;

    /**
     * Carga el fichero de configuración. Es privado para que solo se pueda crear desde
     * {@link #getInstance()}.
     *
     * @throws AppException si el fichero no está en el classpath o no se puede leer
     */
    private PropertiesReader() throws AppException {
        p = new Properties();

        // Usamos try-with-resources para asegurar que el fichero se cierra
        try (InputStream in = PropertiesReader.class.getClassLoader().getResourceAsStream(FICHERO)) {
            // getResourceAsStream no lanza excepción si no lo encuentra, devuelve null
            if (in == null) {
                throw new AppException("No se encuentra " + FICHERO + " en src/main/resources");
            }
            p.load(new InputStreamReader(in, StandardCharsets.UTF_8));

        } catch (IOException e) {
            // no lo muestro aquí: lo lanzo y que sea el menú quien muestre el mensaje
            throw new AppException("No se ha podido leer " + FICHERO + ": " + e.getMessage(), e);
        }
    }

    /**
     * Devuelve la única instancia, creándola (y leyendo el fichero) la primera vez.
     *
     * @return la instancia de {@code PropertiesReader}
     * @throws AppException si el fichero no está o no se puede leer
     */
    public static PropertiesReader getInstance() throws AppException {
        if (instance == null) {
            instance = new PropertiesReader();
        }

        return instance;
    }

    /**
     * Devuelve el valor de una propiedad del fichero.
     *
     * @param propiedad nombre de la propiedad, por ejemplo {@code mysql.url}
     * @return su valor, o {@code null} si no está en el fichero
     */
    public String get(String propiedad) {
        return p.getProperty(propiedad);
    }
}
