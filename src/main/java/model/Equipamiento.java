package model;

import exceptions.CocheException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public void annadirExtra(String extra) throws CocheException {
        if (equipamiento.stream().anyMatch(e -> e.equalsIgnoreCase(extra))) {
            throw new CocheException("Este extra ya existe");
        }
        equipamiento.add(extra);
    }

    public void borrarExtra(String extra) throws CocheException {
        if (!equipamiento.removeIf(e -> e.equalsIgnoreCase(extra))) {
            throw new CocheException("Este extra " + extra + " no existe");
        }
    }

    public String modificarExtra(String extras, String nuevoExtra) throws CocheException {
        if (equipamiento.stream().anyMatch(e -> e.equalsIgnoreCase(nuevoExtra))) {
            throw new CocheException("El extra " + nuevoExtra + " ya existe");
        }
        String encontrado = equipamiento.stream()
                .filter(e -> e.equalsIgnoreCase(extras))
                .findFirst()
                .orElseThrow(() -> new CocheException("Este extra " + extras + " no existe"));
        equipamiento.set(equipamiento.indexOf(encontrado), nuevoExtra);
        return nuevoExtra;
    }

    public List<String> ordenarExtras() {
        return equipamiento.stream()
                .sorted((e1, e2) -> e1.compareToIgnoreCase(e2))
                .collect(Collectors.toList());
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
