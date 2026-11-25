package model;

/**
 * Representa a un propietario (cliente) del concesionario.
 * <p>
 * Se corresponde con la tabla {@code propietarios}. El {@code idPropietario} es la clave
 * primaria autoincremental y el {@code dni} es un campo único (no puede haber dos
 * propietarios con el mismo DNI).
 */
public class Propietario {
    private int idPropietario;
    private String dni;
    private String nombre;
    private String apellidos;
    private String telefono;

    /**
     * Constructor para crear un propietario nuevo. No lleva id porque es autoincremental
     * y lo genera la BD al hacer el INSERT.
     *
     * @param dni       DNI del propietario (único)
     * @param nombre    nombre
     * @param apellidos apellidos
     * @param telefono  teléfono de contacto
     */
    public Propietario(String dni, String nombre, String apellidos, String telefono) {
        this.dni = dni;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.telefono = telefono;
    }

    /**
     * Constructor para cuando leo el propietario de la BD con un SELECT, aquí ya tiene id.
     * Reutiliza el constructor de arriba con {@code this(...)}.
     *
     * @param idPropietario id generado por la BD
     * @param dni           DNI del propietario
     * @param nombre        nombre
     * @param apellidos     apellidos
     * @param telefono      teléfono de contacto
     */
    public Propietario(int idPropietario, String dni, String nombre, String apellidos, String telefono) {
        this(dni, nombre, apellidos, telefono);
        this.idPropietario = idPropietario;
    }

    public int getIdPropietario() {
        return idPropietario;
    }

    public void setIdPropietario(int idPropietario) {
        this.idPropietario = idPropietario;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Propietario ===\n")
          .append("  ID        : ").append(idPropietario).append("\n")
          .append("  DNI       : ").append(dni).append("\n")
          .append("  Nombre    : ").append(nombre).append("\n")
          .append("  Apellidos : ").append(apellidos).append("\n")
          .append("  Teléfono  : ").append(telefono);
        return sb.toString();
    }
}
