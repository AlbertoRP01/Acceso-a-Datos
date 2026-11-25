package model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa un coche del concesionario. Se corresponde con la tabla {@code coches}.
 * <p>
 * La matrícula es la clave primaria (no hace falta autoincrement porque ya viene dada).
 * El {@code idPropietario} es la clave foránea a {@code propietarios}: vale {@code null}
 * mientras el coche pertenece al concesionario y se rellena al hacer un traspaso.
 */
public class Coche {
    private String matricula;
    private String marca;
    private String modelo;
    private List<Extra> extras;
    private BigDecimal precio;
    private Integer idPropietario;   // Integer y no int para poder guardar null (sin propietario)
    private String dniPropietario;   // no es columna de coches: solo se rellena en el listado con JOIN (opción 7)

    /**
     * Constructor para un coche nuevo: todavía no tiene propietario, es del concesionario.
     *
     * @param matricula matrícula (clave primaria)
     * @param marca     marca
     * @param modelo    modelo
     * @param precio    precio de venta
     */
    public Coche(String matricula, String marca, String modelo, BigDecimal precio) {
        this.matricula = matricula;
        this.marca = marca;
        this.modelo = modelo;
        this.extras = new ArrayList<>();
        this.precio = precio;
        this.idPropietario = null;
    }

    /**
     * Constructor para cuando leo el coche de la BD, que puede tener ya propietario.
     * Reutiliza el constructor de arriba con {@code this(...)}.
     *
     * @param matricula     matrícula (clave primaria)
     * @param marca         marca
     * @param modelo        modelo
     * @param precio        precio de venta
     * @param idPropietario id del propietario, o {@code null} si es del concesionario
     */
    public Coche(String matricula, String marca, String modelo, BigDecimal precio, Integer idPropietario) {
        this(matricula, marca, modelo, precio);
        this.idPropietario = idPropietario;
    }

    /**
     * Añade un extra (equipamiento) al coche.
     *
     * @param extra extra a añadir
     */
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

    public Integer getIdPropietario() {
        return idPropietario;
    }

    public void setIdPropietario(Integer idPropietario) {
        this.idPropietario = idPropietario;
    }

    public String getDniPropietario() {
        return dniPropietario;
    }

    public void setDniPropietario(String dniPropietario) {
        this.dniPropietario = dniPropietario;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Coche ===\n")
          .append("  Matrícula   : ").append(matricula).append("\n")
          .append("  Marca       : ").append(marca).append("\n")
          .append("  Modelo      : ").append(modelo).append("\n")
          .append("  Precio      : ").append(precio).append(" €\n")
          .append("  Propietario : ").append(textoPropietario()).append("\n")
          .append("  Extras      : ");

        if (extras.isEmpty()) {
            sb.append("sin extras");
        } else {
            for (Extra extra : extras) {
                sb.append("\n    - ").append(extra.getNombre());
            }
        }
        return sb.toString();
    }

    /**
     * Método auxiliar del toString: si tengo el DNI (listado con JOIN) lo muestro,
     * si no, el id; y si no tiene propietario, "concesionario".
     */
    private String textoPropietario() {
        if (idPropietario == null) {
            return "concesionario";
        }
        return dniPropietario != null ? "DNI " + dniPropietario : "id " + idPropietario;
    }
}
