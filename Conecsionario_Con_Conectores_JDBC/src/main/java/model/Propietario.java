package model;

public class Propietario {
    private int idPropietario;
    private String nombre;
    private String apellidos;
    private String telefono;

    // para crear un propietario nuevo, sin id porque es autoincremental y lo genera la BD
    public Propietario(String nombre, String apellidos, String telefono) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.telefono = telefono;
    }

    // para cuando lo leo de la BD con el SELECT, aqui ya tiene id
    public Propietario(int idPropietario, String nombre, String apellidos, String telefono) {
        this.idPropietario = idPropietario;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.telefono = telefono;
    }

    public int getIdPropietario() {
        return idPropietario;
    }

    public void setIdPropietario(int idPropietario) {
        this.idPropietario = idPropietario;
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
          .append("  Nombre    : ").append(nombre).append("\n")
          .append("  Apellidos : ").append(apellidos).append("\n")
          .append("  Teléfono  : ").append(telefono);
        return sb.toString();
    }
}
