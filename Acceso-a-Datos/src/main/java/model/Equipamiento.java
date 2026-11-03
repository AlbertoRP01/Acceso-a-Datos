package model;

import exceptions.CocheException;

import jakarta.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Equipamiento de un coche: la lista de extras que genera la etiqueta {@code <equipamiento>}
 * con un {@code <extra>} por cada elemento.
 */
public class Equipamiento {
    private List<String> extra;

    /**
     * Constructor vacío obligatorio para JAXB. Inicializa la lista para que nunca sea null.
     */
    public Equipamiento() {
        this.extra = new ArrayList<>();
    }

    /**
     * @return la lista de extras
     */
    @XmlElement(name = "extra")
    public List<String> getExtra() {
        return extra;
    }

    /**
     * @param extra nueva lista de extras
     */
    public void setExtra(List<String> extra) {
        this.extra = extra;
    }

    /**
     * Añade un extra si no está vacío y no existe ya (sin distinguir mayúsculas).
     *
     * @param extra nombre del extra
     * @throws CocheException si el extra está vacío o ya existe
     */
    public void annadirExtra(String extra) throws CocheException {
        // Evita extras vacíos, por ejemplo de una línea del CSV que acaba en ';'
        if (extra == null || extra.isBlank()) {
            throw new CocheException("El extra no puede estar vacío");
        }
        if (this.extra.stream().anyMatch(e -> e.equalsIgnoreCase(extra))) {
            throw new CocheException("Este extra ya existe");
        }
        this.extra.add(extra.trim());
    }

    /**
     * Borra un extra (sin distinguir mayúsculas).
     *
     * @param extra nombre del extra a borrar
     * @throws CocheException si el extra no existe
     */
    public void borrarExtra(String extra) throws CocheException {
        if (!this.extra.removeIf(e -> e.equalsIgnoreCase(extra))) {
            throw new CocheException("Este extra " + extra + " no existe");
        }
    }

    /**
     * Cambia el nombre de un extra.
     *
     * @param extraViejo nombre actual del extra
     * @param nuevoExtra nuevo nombre del extra
     * @return el nuevo nombre del extra
     * @throws CocheException si algún nombre está vacío, el viejo no existe o el nuevo ya existe
     */
    public String modificarExtra(String extraViejo, String nuevoExtra) throws CocheException {
        if (extraViejo == null || extraViejo.trim().isEmpty()) {
            throw new CocheException("Debe seleccionar el extra que quiere modificar");
        }
        if (nuevoExtra == null || nuevoExtra.trim().isEmpty()) {
            throw new CocheException("Debe indicar el nuevo nombre del extra");
        }
        String encontrado = extra.stream()
                .filter(e -> e.equalsIgnoreCase(extraViejo))
                .findFirst()
                .orElseThrow(() -> new CocheException("El extra " + extraViejo + " no existe"));

        // Se ignora el propio extra viejo para permitir, por ejemplo, cambiar solo mayúsculas/minúsculas
        if (extra.stream()
                .filter(e -> !e.equalsIgnoreCase(encontrado))
                .anyMatch(e -> e.equalsIgnoreCase(nuevoExtra))) {
            throw new CocheException("El extra " + nuevoExtra + " ya existe");
        }

        extra.replaceAll(e -> e.equalsIgnoreCase(encontrado) ? nuevoExtra : e);
        return nuevoExtra;
    }

    /**
     * @return una copia de los extras ordenados alfabéticamente (sin distinguir mayúsculas)
     */
    public List<String> ordenarExtras() {
        return extra.stream()
                .sorted((e1, e2) -> e1.compareToIgnoreCase(e2))
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Equipamiento that = (Equipamiento) o;
        return Objects.equals(extra, that.extra);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(extra);
    }
}
