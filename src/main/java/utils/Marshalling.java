package utils;

import model.Coche;
import model.Concesionario;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;

public class Marshalling {
    public static void main(String[] args) {
        try {
            Concesionario concesionario = new Concesionario();
            Coche coche = new Coche();
            concesionario.getCoches().add(coche);
            JAXBContext context = JAXBContext.newInstance(Concesionario.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            StringWriter writer = new StringWriter();
            marshaller.marshal(concesionario, writer);
            String xml = writer.toString();
            System.out.println(xml);
        }catch (JAXBException e){
            e.printStackTrace();
        }
    }
}
