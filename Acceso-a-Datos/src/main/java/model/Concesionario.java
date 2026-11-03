package model;

import exceptions.CocheException;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Base de datos del concesionario: la lista de coches que se guarda en el XML.
 */
@XmlRootElement(name = "concesionario-de-coches")
public class Concesionario {
    private List<Coche> coches;

    /**
     * Constructor vacío obligatorio para JAXB y Jackson.
     */
    public Concesionario() {
        coches = new ArrayList<>();
    }

    /**
     * @return la lista de coches
     */
    @XmlElementWrapper(name = "listado-coches") // etiqueta que envuelve todos los <coche>
    @XmlElement(name = "coche")
    public List<Coche> getCoches() {
        return coches;
    }

    /**
     * Añade un coche y le asigna el siguiente id libre.
     *
     * @param coche coche a añadir
     * @throws CocheException si ya existe un coche con esa matrícula
     */
    public void annadirCoches(Coche coche) throws CocheException {
        // contains usa el equals de Coche, que compara por matrícula
        if (coches.contains(coche)) {
            throw new CocheException("Ya existe un coche con matrícula " + coche.getMatriculaCoche());
        }
        coche.setId(siguienteId());
        coches.add(coche);
    }

    /**
     * Borra el coche con la matrícula indicada.
     *
     * @param matricula matrícula del coche a borrar
     * @throws CocheException si no existe
     */
    public void eliminarCoche(String matricula) throws CocheException {
        coches.remove(buscarPorMatricula(matricula));
    }

    /**
     * Modifica marca, modelo y extras de un coche. La matrícula no se puede cambiar.
     *
     * @param matricula matrícula del coche a modificar
     * @param marca     nueva marca
     * @param modelo    nuevo modelo
     * @param extras    nuevos extras (puede ser null o vacía)
     * @return el coche modificado
     * @throws CocheException si no existe o algún dato no es válido
     */
    public Coche modificarCoche(String matricula, String marca, String modelo,
                                List<String> extras) throws CocheException {
        Coche coche = buscarPorMatricula(matricula);
        // Se prepara el equipamiento antes de tocar el coche: si un extra no es válido, el coche no cambia
        Equipamiento nuevo = new Equipamiento();
        if (extras != null) {
            extras.forEach(nuevo::annadirExtra);
        }
        coche.setMarca(marca);
        coche.setModelo(modelo);
        coche.setEquipamiento(nuevo);
        return coche;
    }

    /**
     * Busca un coche por matrícula (sin distinguir mayúsculas).
     *
     * @param matricula matrícula a buscar
     * @return el coche encontrado
     * @throws CocheException si no existe
     */
    public Coche buscarPorMatricula(String matricula) throws CocheException {
        return coches.stream()
                .filter(c -> c.getMatriculaCoche().equalsIgnoreCase(matricula))
                .findFirst()
                .orElseThrow(() -> new CocheException("No existe el coche con matrícula " + matricula));
    }

    /**
     * Ordena los coches por matrícula (sin distinguir mayúsculas).
     */
    public void ordenarPorMatricula() {
        coches.sort(Comparator.comparing(Coche::getMatriculaCoche, String.CASE_INSENSITIVE_ORDER));
    }

    /**
     * Calcula el siguiente id: el mayor existente + 1 (o 1 si no hay coches).
     */
    private int siguienteId() {
        return coches.stream().mapToInt(Coche::getId).max().orElse(0) + 1;
    }
}
