package model;
//import javax.xml.bind.annotation.XmlRootElement;

import exceptions.CocheException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class Concesionario {
    private List<Coche> coches;

    public Concesionario() {
        coches = new ArrayList<>();
    }

    @XmlElement(name = "coche")
    public List<Coche> getCoches() {
        return coches;
    }

    public void annadirCoches(Coche coche) throws CocheException {
        if (coches.stream().anyMatch(c -> c.equals(coche))) {
            throw new CocheException("Este coche ya existe");
        }
        coches.add(coche);
    }
    public void eliminarCoche(Coche coche)throws CocheException{
        if (!coches.removeIf(e -> e.equals(coche))){
            throw  new CocheException("Este coche No existe");
        }
    }
    public Coche modificarCoche(Coche cocheViejo, Coche cocheNuevo) throws CocheException {
        if (cocheViejo == null) {
            throw new CocheException("Debe seleccionar el coche que quiere modificar");
        }
        if (cocheNuevo == null) {
            throw new CocheException("Debe indicar los nuevos datos del coche");
        }
        Coche encontrado = coches.stream()
                .filter(c -> c.equals(cocheViejo))
                .findFirst()
                .orElseThrow(() -> new CocheException("El coche " + cocheViejo.getMatriculaCoche() + " no existe"));

        // Se ignora el propio coche viejo para permitir modificarlo manteniendo su identidad
        if (coches.stream()
                .filter(c -> c != encontrado)
                .anyMatch(c -> c.equals(cocheNuevo))) {
            throw new CocheException("El coche " + cocheNuevo.getMatriculaCoche() + " ya existe");
        }

        coches.replaceAll(c -> c == encontrado ? cocheNuevo : c);
        return cocheNuevo;
    }

}