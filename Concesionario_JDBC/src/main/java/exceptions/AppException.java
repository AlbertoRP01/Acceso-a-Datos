package exceptions;

/**
 * Excepción propia de la aplicación. Se usa para avisar de errores (por ejemplo, que no
 * se encuentra {@code config.properties}) con un mensaje que se pueda mostrar al usuario
 * en el menú.
 */
public class AppException extends Exception {

    /**
     * @param message mensaje del error, pensado para mostrarlo al usuario
     */
    public AppException(String message) {
        super(message);
    }

    /**
     * Para envolver otra excepción (por ejemplo, una {@code IOException} o una
     * {@code SQLException}) sin perder la original, que sirve para depurar.
     *
     * @param message mensaje del error, pensado para mostrarlo al usuario
     * @param cause   excepción original que lo ha provocado
     */
    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}
