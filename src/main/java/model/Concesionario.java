package model;
//import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
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
    public void annadirCoches(){

    }
}
