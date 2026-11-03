package utils;


import model.Coche
;
import model.Concesionario;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;

public class Gestor {

    public void marshalling() {
        try {
            Concesionario concesionario = new Concesionario();
            Coche coche = new Coche();
            concesionario.getCoches().add(coche);
            JAXBContext context = JAXBContext.newInstance(Concesionario.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            File archivoXML = new File("concesionario_plantilla.xml");
            marshaller.marshal(concesionario, archivoXML);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public void unmarshalling() {
        try {
            Concesionario concesionario = new Concesionario();
            Coche coche = new Coche();
            concesionario.getCoches().add(coche);
            JAXBContext context = JAXBContext.newInstance(Concesionario.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            File archivoXML = new File("concesionario_plantilla.xml");
            Concesionario concesionarioXML = (Concesionario) unmarshaller.unmarshal(archivoXML);
            System.out.println("Concesionario cargado: " + concesionarioXML.getCoches().size());
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public void serializar() {
        Concesionario concesionario = new Concesionario();
        Coche coche = new Coche();
        concesionario.annadirCoches(coche);
        JsonbConfig config = new JsonbConfig().withFormatting(true);
        Jsonb jsonb = JsonbBuilder.create(config);
        System.out.println(jsonb.toJson(concesionario));
    }
    public  void leerCsv(Concesionario concesionario){
        try (BufferedReader br = Files.newBufferedReader()){

        }
    }
}
