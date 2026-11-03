package model;

import exceptions.CocheException;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.List;
import java.util.Objects;

/**
 * Representa un coche del concesionario.
 * La matrícula es la clave: identifica al coche y no se puede modificar.
 */
@XmlAccessorType(XmlAccessType.FIELD) // JAXB lee y escribe los atributos directamente, sin necesitar setters
@XmlType(propOrder = {"matriculaCoche", "marca", "modelo", "equipamiento"}) // nombres de los ATRIBUTOS Java, no de las etiquetas
public class Coche {

    @XmlAttribute // <coche id="1">: es atributo de la etiqueta, no un elemento hijo
    private int id;

    @XmlElement(name = "matricula-coche") // el nombre de la etiqueta no coincide con el del atributo
    private String matriculaCoche;

    private String marca;

    private String modelo;

    @XmlElement(name = "equipamiento") // un solo objeto por coche; los <extra> van dentro de Equipamiento
    private Equipamiento equipamiento = new Equipamiento(); // inicializado: un coche sin extras genera <equipamiento/>

    /**
     * Constructor vacío. Lo usan JAXB y Jackson para crear el objeto antes de rellenarlo.
     * No valida nada porque todavía no hay datos.
     */
    public Coche() {
    }

    /**
     * Crea un coche validando sus datos. Se usa al insertar desde el menú o al importar el CSV.
     *
     * @param matriculaCoche matrícula del coche (clave, no puede estar vacía)
     * @param marca          marca del coche
     * @param modelo         modelo del coche
     * @param extras         lista de extras; puede ser null o vacía
     * @throws CocheException si la matrícula, la marca o el modelo están vacíos
     */
    public Coche(String matriculaCoche, String marca, String modelo, List<String> extras) throws CocheException {
        // La matrícula solo se asigna aquí: no existe setMatriculaCoche porque no se puede modificar
        this.matriculaCoche = validarTexto(matriculaCoche, "matrícula");
        setMarca(marca);
        setModelo(modelo);
        if (extras != null) {
            extras.forEach(equipamiento::annadirExtra);
        }
    }

    /**
     * Comprueba que un texto no sea nulo ni esté vacío. Método auxiliar para no repetir la validación.
     *
     * @param valor texto a comprobar
     * @param campo nombre del campo, para el mensaje de error
     * @return el texto sin espacios al principio ni al final
     * @throws CocheException si el texto es nulo o está vacío
     */
    private static String validarTexto(String valor, String campo) throws CocheException {
        if (valor == null || valor.isBlank()) {
            throw new CocheException("El coche debe tener " + campo);
        }
        return valor.trim();
    }

    /**
     * @return el identificador numérico del coche
     */
    public int getId() {
        return id;
    }

    /**
     * Asigna el identificador. Lo usa el concesionario al añadir coches nuevos.
     *
     * @param id identificador numérico
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return la matrícula del coche
     */
    public String getMatriculaCoche() {
        return matriculaCoche;
    }

    /**
     * @return la marca del coche
     */
    public String getMarca() {
        return marca;
    }

    /**
     * @param marca nueva marca del coche
     * @throws CocheException si la marca está vacía
     */
    public void setMarca(String marca) throws CocheException {
        this.marca = validarTexto(marca, "marca");
    }

    /**
     * @return el modelo del coche
     */
    public String getModelo() {
        return modelo;
    }

    /**
     * @param modelo nuevo modelo del coche
     * @throws CocheException si el modelo está vacío
     */
    public void setModelo(String modelo) throws CocheException {
        this.modelo = validarTexto(modelo, "modelo");
    }

    /**
     * @return el equipamiento del coche (nunca null)
     */
    public Equipamiento getEquipamiento() {
        return equipamiento;
    }

    /**
     * Sustituye el equipamiento del coche (útil al modificar un registro).
     *
     * @param equipamiento nuevo equipamiento; si es null se deja uno vacío
     */
    public void setEquipamiento(Equipamiento equipamiento) {
        this.equipamiento = (equipamiento == null) ? new Equipamiento() : equipamiento;
    }

    /**
     * Dos coches son iguales si tienen la misma matrícula (sin distinguir mayúsculas).
     * Es lo que permite al concesionario detectar matrículas duplicadas.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coche otro)) return false;
        return matriculaCoche != null && matriculaCoche.equalsIgnoreCase(otro.matriculaCoche);
    }

    /**
     * Coherente con equals: se calcula a partir de la matrícula en minúsculas.
     */
    @Override
    public int hashCode() {
        return matriculaCoche == null ? 0 : Objects.hash(matriculaCoche.toLowerCase());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Coche: \n");
        sb.append("id: ").append(getId()).append("\n");
        sb.append("Matricula: ").append(getMatriculaCoche()).append("\n");
        sb.append("Marca: ").append(getMarca()).append("\n");
        sb.append("Modelo: ").append(getModelo()).append("\n");
        sb.append("Equipamiento: ").append(getEquipamiento().getExtra());
        return sb.toString();
    }
}
