package model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Representa un traspaso (venta) de un coche. Se corresponde con la tabla {@code traspasos}.
 * <p>
 * El {@code idVendedor} vale {@code null} cuando el que vende es el concesionario (el coche
 * todavía no tenía propietario). El comprador siempre es un propietario registrado.
 */
public class Traspaso {
    private int idTraspaso;
    private String matriculaCoche;   // FK a la tabla coches
    private Integer idVendedor;      // FK a propietarios. Integer y no int: null = lo vende el concesionario
    private int idComprador;         // FK a propietarios
    private BigDecimal montoEconomico;
    private LocalDate fecha;

    /**
     * Constructor para un traspaso nuevo: el id lo pone la BD y lo recojo luego con
     * {@code getGeneratedKeys}.
     *
     * @param matriculaCoche matrícula del coche traspasado
     * @param idVendedor     id del propietario que vende, o {@code null} si vende el concesionario
     * @param idComprador    id del propietario que compra
     * @param montoEconomico importe del traspaso
     * @param fecha          fecha del traspaso
     */
    public Traspaso(String matriculaCoche, Integer idVendedor, int idComprador,
                    BigDecimal montoEconomico, LocalDate fecha) {
        this.matriculaCoche = matriculaCoche;
        this.idVendedor = idVendedor;
        this.idComprador = idComprador;
        this.montoEconomico = montoEconomico;
        this.fecha = fecha;
    }

    /**
     * Constructor para cuando lo leo de la BD. Reutiliza el de arriba con {@code this(...)}.
     *
     * @param idTraspaso     id del traspaso
     * @param matriculaCoche matrícula del coche traspasado
     * @param idVendedor     id del propietario que vende, o {@code null} si vende el concesionario
     * @param idComprador    id del propietario que compra
     * @param montoEconomico importe del traspaso
     * @param fecha          fecha del traspaso
     */
    public Traspaso(int idTraspaso, String matriculaCoche, Integer idVendedor, int idComprador,
                    BigDecimal montoEconomico, LocalDate fecha) {
        this(matriculaCoche, idVendedor, idComprador, montoEconomico, fecha);
        this.idTraspaso = idTraspaso;
    }

    public int getIdTraspaso() {
        return idTraspaso;
    }

    public void setIdTraspaso(int idTraspaso) {
        this.idTraspaso = idTraspaso;
    }

    public String getMatriculaCoche() {
        return matriculaCoche;
    }

    public void setMatriculaCoche(String matriculaCoche) {
        this.matriculaCoche = matriculaCoche;
    }

    public Integer getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(Integer idVendedor) {
        this.idVendedor = idVendedor;
    }

    public int getIdComprador() {
        return idComprador;
    }

    public void setIdComprador(int idComprador) {
        this.idComprador = idComprador;
    }

    public BigDecimal getMontoEconomico() {
        return montoEconomico;
    }

    public void setMontoEconomico(BigDecimal montoEconomico) {
        this.montoEconomico = montoEconomico;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Traspaso ===\n")
          .append("  ID         : ").append(idTraspaso).append("\n")
          .append("  Matrícula  : ").append(matriculaCoche).append("\n")
          .append("  Vendedor   : ").append(idVendedor != null ? idVendedor : "Concesionario").append("\n")
          .append("  Comprador  : ").append(idComprador).append("\n")
          .append("  Importe    : ").append(montoEconomico).append(" €\n")
          .append("  Fecha      : ").append(fecha);
        return sb.toString();
    }
}
