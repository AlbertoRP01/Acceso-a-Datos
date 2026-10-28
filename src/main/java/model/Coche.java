package model;

import exceptions.CocheException;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@XmlType(propOrder = {"id", "matriculaCoche", "marca", "modelo", "equipamiento"})
public class Coche {
    private static int idNext = 1;
    private int id;
    private String matriculaCoche;
    private String marca;
    private String modelo;
    private List<String> equipamiento;

    public Coche(String matriculaCoche, String marca, String modelo) {
        this.id = idNext++;
        this.matriculaCoche = matriculaCoche;
        this.marca = marca;
        this.modelo = modelo;
        this.equipamiento = new ArrayList<>();

    }


    @XmlElement(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    @XmlElement(name = "matricula")
    public String getMatriculaCoche() {
        return matriculaCoche;
    }

    public void setMatriculaCoche(String matriculaCoche) {
        this.matriculaCoche = matriculaCoche;
    }


    @XmlElement(name = "marca")
    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }


    @XmlElement(name = "modelo")
    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }


    @XmlElement(name = "equipamiento")
    public List<String> getEquipamiento() {
        return equipamiento;
    }

    public void setEquipamiento(List<String> equipamiento) {
        this.equipamiento = equipamiento;
    }

    public void annadirEquipamiento( String equipamientos) throws CocheException {
        if (equipamiento.stream().noneMatch(e-> e.equalsIgnoreCase(equipamientos))){
            equipamiento.add(equipamientos);
        }
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Coche: \n");
        sb.append("id: ").append(getId()).append("\n");
        sb.append("Matricula: ").append(getMatriculaCoche()).append("\n");
        sb.append("Marca: ").append(getMarca()).append("\n");
        sb.append("Modelo: ").append(getModelo()).append("\n");
        sb.append("Equipamiento: ").append(getEquipamiento());
        return sb.toString();
    }

}
