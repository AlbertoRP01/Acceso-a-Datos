package utils;

import model.Coche;
import model.Concesionario;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;

public class Gestor {

    public void marshalling(){
        try {
            Concesionario concesionario = new Concesionario();
            Coche coche = new Coche();
            concesionario.getCoches().add(coche);
            JAXBContext context = JAXBContext.newInstance(Concesionario.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            File archivoXML = new File("concesionario_plantilla.xml");
            marshaller.marshal(concesionario, archivoXML);
        }catch (JAXBException e){
            e.printStackTrace();
        }
    }
    public void unmarshalling(){
        try {
            Concesionario concesionario = new Concesionario();
            Coche coche = new Coche();
            concesionario.getCoches().add(coche);
            JAXBContext context = JAXBContext.newInstance(Concesionario.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            File archivoXML = new File("concesionario_plantilla.xml");
            Concesionario concesionarioXML = (Concesionario) unmarshaller.unmarshal(archivoXML);
            System.out.println("Concesionario cargado: " + concesionarioXML.getCoches().size());
        }catch (JAXBException e) {
             e.printStackTrace();
        }
    }
    public void serializar(){
        Concesionario concesionario = new Concesionario();
        Coche coche = new Coche();
        concesionario.annadirCoches(coche);
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT); // JSON con formato
        try {
            System.out.println(mapper.writeValueAsString(concesionario));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }


}
