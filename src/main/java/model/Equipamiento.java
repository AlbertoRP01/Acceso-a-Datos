package model;

import exceptions.CocheException;

import java.util.ArrayList;
import java.util.List;

public class Equipamiento {
    private String extra;
    private List<String> equipamiento;

    public Equipamiento(String extra) {
        this.equipamiento = new ArrayList<>();
        this.extra = extra;
    }

    public String getExtra() {
        return extra;
    }

    public List<String> getEquipamiento() {
        return equipamiento;
    }

    public void setEquipamiento(List<String> equipamiento) {
        this.equipamiento = equipamiento;
    }

    public void annadirEquipamiento(String extra) throws CocheException{
        if (equipamiento.stream().anyMatch(e -> e.equalsIgnoreCase(extra))){
            throw new CocheException("Este extra ya existe");
        }
        equipamiento.add(extra);
    }
    public void borrarPieza(String extra) throws CocheException{
        if (!equipamiento.removeIf(e -> e.equalsIgnoreCase(extra))){
            throw new CocheException("Este extra " + extra + " no existe");
        }
    }
    public String modificarExtra(String extras, String nuevoExtra) throws CocheException{
        if (equipamiento.stream().anyMatch(e -> e.equalsIgnoreCase(nuevoExtra))){
            throw new CocheException("El extra " + nuevoExtra + " ya existe");
        }
        String encontrado = equipamiento.stream()
                .filter(e -> e.equalsIgnoreCase(extras))
                .findFirst()
                .orElseThrow(() -> new CocheException("Este extra " + extras + " no existe"));
        equipamiento.set(equipamiento.indexOf(encontrado), nuevoExtra);
        return nuevoExtra;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Equipamiento)) return false;
        Equipamiento that = (Equipamiento) o;
        return extra != null && extra.equalsIgnoreCase(that.extra);
    }

    @Override
    public int hashCode() {
        return extra == null ? 0 : extra.toLowerCase().hashCode();
    }
}
