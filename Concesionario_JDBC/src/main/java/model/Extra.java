package model;

public class Extra {
    private int idExtra;
    private String nombre;

    // este lo uso cuando leo el extra del CSV, todavia no tiene id porque lo pone la BD al insertarlo
    public Extra(String nombre) {
        this.nombre = nombre;
    }

    // y este cuando lo saco de la BD, que ya viene con su id
    public Extra(int idExtra, String nombre) {
        this.idExtra = idExtra;
        this.nombre = nombre;
    }

    public int getIdExtra() {
        return idExtra;
    }

    public void setIdExtra(int idExtra) {
        this.idExtra = idExtra;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Extra [").append(idExtra).append("] ")
          .append(nombre);
        return sb.toString();
    }
}
