package model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Coche {
    private String matricula;        // la matricula es la clave primaria, no hace falta autoincrement porque ya viene dada
    private String marca;
    private String modelo;
    private List<Extra> extras;
    private BigDecimal precio;

    public Coche(String matricula, String marca, String modelo, BigDecimal precio) {
        this.matricula = matricula;
        this.marca = marca;
        this.modelo = modelo;
        this.extras = new ArrayList<>();
        this.precio = precio;
    }

    public void addExtra(Extra extra) {
        extras.add(extra);
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public List<Extra> getExtras() {
        return extras;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Coche ===\n")
          .append("  Matrícula : ").append(matricula).append("\n")
          .append("  Marca     : ").append(marca).append("\n")
          .append("  Modelo    : ").append(modelo).append("\n")
          .append("  Precio    : ").append(precio).append(" €\n")
          .append("  Extras    : ");

        if (extras.isEmpty()) {
            sb.append("sin extras");
        } else {
            for (Extra extra : extras) {
                sb.append("\n    - ").append(extra.getNombre());
            }
        }
        return sb.toString();
    }
}
