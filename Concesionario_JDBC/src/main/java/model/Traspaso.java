package model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Traspaso {
    private int idTraspaso;
    private String matriculaCoche;   // FK a la tabla coche
    private int idVendedor;          // FK a propietario
    private int idComprador;         // FK a propietario
    private BigDecimal montoEconomico;
    private LocalDate fecha;

    // constructor para un traspaso nuevo, el id lo saco luego con getGeneratedKeys
    public Traspaso(String matriculaCoche, int idVendedor, int idComprador,
                    BigDecimal montoEconomico, LocalDate fecha) {
        this.matriculaCoche = matriculaCoche;
        this.idVendedor = idVendedor;
        this.idComprador = idComprador;
        this.montoEconomico = montoEconomico;
        this.fecha = fecha;
    }

    // constructor para cuando lo leo de la BD (reutilizo el de arriba con this)
    public Traspaso(int idTraspaso, String matriculaCoche, int idVendedor, int idComprador,
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

    public int getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(int idVendedor) {
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
          .append("  Vendedor   : ").append(idVendedor).append("\n")
          .append("  Comprador  : ").append(idComprador).append("\n")
          .append("  Importe    : ").append(montoEconomico).append(" €\n")
          .append("  Fecha      : ").append(fecha);
        return sb.toString();
    }
}
