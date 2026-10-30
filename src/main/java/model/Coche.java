package model;

import exceptions.CocheException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@XmlType(propOrder = {"id", "matriculaCoche", "marca", "modelo", "equipamiento"})
public class Coche {
    private static int idNext = 1;
    private int id;
    private String matriculaCoche;
    private String marca;
    private String modelo;
    private List<Equipamiento> equipamientos;

    public Coche() {
        this.id = idNext++;
        setMatriculaCoche(this.matriculaCoche); ;
        setMarca(this.matriculaCoche);
        setModelo(this.modelo);
        equipamientos = new ArrayList<>();
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
        if (matriculaCoche == null || matriculaCoche.isEmpty()){
            throw new CocheException("El coche debe de tener matricula");
        }
        this.matriculaCoche = matriculaCoche;
    }


    @XmlElement(name = "marca")
    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        if (marca == null || marca.isEmpty()){
            throw new CocheException("El coche debe de tener marca");
        }
        this.marca = marca;
    }


    @XmlElement(name = "modelo")
    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        if (modelo == null || modelo.isEmpty()){
            throw new CocheException("El coche debe de tener modelo");
        }
        this.modelo = modelo;
    }


    @XmlElement(name = "equipamiento")
    public List<Equipamiento> getEquipamientos() {
        return equipamientos;
    }

    public void setEquipamientos(List<Equipamiento> equipamientos) {
        this.equipamientos = equipamientos;
    }

    public void annadirEquipamiento(Equipamiento equipamientos) throws CocheException {
        if (this.equipamientos.stream().anyMatch(e -> e.equals(equipamientos))) {
            throw new CocheException("Este Equipamiento ya existe");
        }
        this.equipamientos.add(equipamientos);
    }

    public void borrar(Equipamiento equipamientos) throws CocheException {
        if (!this.equipamientos.removeIf(e -> e.equals(equipamientos))) {
            throw new CocheException("El equipamiento no existe en el coche");
        }
    }

    public Equipamiento modificarEquipamiento(Equipamiento equipamientoViejo, Equipamiento equipamientoNuevo) throws CocheException{
        if (equipamientos.stream().anyMatch(e -> e.equals(equipamientoNuevo))){
            throw new CocheException("El equipamiento" + equipamientoNuevo + " Ya existe");
        }
        Equipamiento encontrado = equipamientos.stream()
                .filter(e -> e.equals(equipamientoViejo))
                .findFirst()
                .orElseThrow( () -> new CocheException("Ese Equipamiento"  + equipamientoViejo + " No existe"));
                    equipamientos.set(equipamientos.indexOf(encontrado), equipamientoNuevo);
                    return equipamientoNuevo;
    }

    public List<Equipamiento> ordenarEquipamiento(){
        return equipamientos.stream()
                .sorted((e1, e2) -> e1.getExtra().compareToIgnoreCase(e2.getExtra()))
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Coche: \n");
        sb.append("id: ").append(getId()).append("\n");
        sb.append("Matricula: ").append(getMatriculaCoche()).append("\n");
        sb.append("Marca: ").append(getMarca()).append("\n");
        sb.append("Modelo: ").append(getModelo()).append("\n");
        sb.append("Equipamiento: ").append(getEquipamientos());
        return sb.toString();
    }

}
