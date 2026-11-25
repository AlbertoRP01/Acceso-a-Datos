package service;

import dao.CocheDAO;
import dao.PropietarioDAO;
import dao.TraspasoDAO;
import exceptions.AppException;
import model.Coche;
import model.Propietario;
import model.Traspaso;
import utils.ConexionDB;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Realiza el traspaso (venta) de un coche a un comprador (opción 10 del menú).
 * <p>
 * El traspaso toca dos tablas: cambia el propietario en {@code coches} e inserta el
 * registro en {@code traspasos}. Por eso va en una transacción: o se hacen las dos
 * cosas, o ninguna. Los DAO no hacen commit; lo controla este service, que es el que
 * sabe dónde empieza y dónde acaba la operación completa.
 */
public class TraspasoService {

    /**
     * Hace el traspaso de un coche al comprador indicado.
     * <ol>
     *     <li>Comprueba que el comprador existe (por DNI).</li>
     *     <li>Comprueba que el coche existe (por matrícula) y que no es ya del comprador.</li>
     *     <li>En una transacción: el comprador pasa a ser el dueño del coche y se registra
     *     el traspaso. El vendedor es el dueño anterior ({@code null} si era del concesionario).</li>
     * </ol>
     *
     * @param con       conexión abierta
     * @param dni       DNI del comprador
     * @param matricula matrícula del coche
     * @param monto     importe del traspaso
     * @return el traspaso realizado, con su id
     * @throws AppException si el comprador o el coche no existen, el importe no es válido
     *                      o falla la BD (en ese caso se deshace todo)
     */
    public static Traspaso realizar(Connection con, String dni, String matricula, BigDecimal monto)
            throws AppException {
        PropietarioDAO propietarioDAO = new PropietarioDAO(con);
        CocheDAO cocheDAO = new CocheDAO(con);
        TraspasoDAO traspasoDAO = new TraspasoDAO(con);

        // signum() devuelve -1, 0 o 1 según el signo: el importe tiene que ser positivo
        if (monto.signum() <= 0) {
            throw new AppException("El importe tiene que ser mayor que 0");
        }

        try {
            // 1. y 2. Comprobaciones: se hacen antes de empezar la transacción
            Propietario comprador = propietarioDAO.buscarPorDni(dni);
            if (comprador == null) {
                throw new AppException("No existe ningún propietario con DNI " + dni + ". Regístralo primero (opción 3)");
            }
            Coche coche = cocheDAO.buscarPorMatricula(matricula);
            if (coche == null) {
                throw new AppException("No existe ningún coche con la matrícula " + matricula);
            }
            // equals y no ==: getIdPropietario() del coche es Integer (puede ser null)
            if (Integer.valueOf(comprador.getIdPropietario()).equals(coche.getIdPropietario())) {
                throw new AppException("El coche " + matricula + " ya es de " + dni);
            }

            // el vendedor es el dueño ANTES del traspaso; hay que leerlo antes del UPDATE
            Traspaso t = new Traspaso(matricula, coche.getIdPropietario(),
                    comprador.getIdPropietario(), monto, LocalDate.now());

            // 3. Transacción: las dos operaciones van juntas
            con.setAutoCommit(false);
            try {
                cocheDAO.cambiarPropietario(matricula, comprador.getIdPropietario());
                traspasoDAO.insertar(t);
                con.commit();
                return t;
            } catch (SQLException e) {
                ConexionDB.deshacer(con);   // si falla una, se deshace también la otra
                throw e;
            } finally {
                ConexionDB.restaurarAutoCommit(con);
            }

        } catch (SQLException e) {
            throw new AppException("No se ha podido hacer el traspaso (no se ha cambiado nada): " + e.getMessage(), e);
        }
    }
}
