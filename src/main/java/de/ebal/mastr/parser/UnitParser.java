package de.ebal.mastr.parser;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

public class UnitParser {

    public static void main(String[] args) {

        try {
            // Normal JAXB RI
            var jaxbContext = JAXBContext.newInstance(SolarUnits.class);
            HashSet<SolarUnit> allUnits = new HashSet<>();

            for (int i = 1; i < 24; i++) {
                System.out.println("Reading file no. " + i);
                File file = new File("C:\\Users\\ebal\\Downloads\\Clustering\\Gesamtdatenexport_20220216__4fb071fd20ce4536972c096279e20424\\EinheitenSolar_" + i + ".xml");

                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

                SolarUnits o = (SolarUnits) jaxbUnmarshaller.unmarshal(file);
                //  var filteredUnits = o.getSolarUnits().stream().filter(unit -> (unit.getFernsteuerbarkeit() != null && unit.getFernsteuerbarkeit() == 1) || unit.getNettonennleistung() >= 100.000).collect(toSet());
                var filteredUnits = o.getSolarUnits().stream() //.filter(unit -> (unit.getFernsteuerbarkeit() != null && unit.getFernsteuerbarkeit() == 1) ||unit.getNettonennleistung() >= 100)
                                     .filter(unit -> unit.getStatus() == 35 && unit.getPostleitzahl() != null && unit.getOrt() != null)
                                     .collect(toSet());
                allUnits.addAll(filteredUnits);
            }
            System.out.println("Number of units currently considered: " + allUnits.size());

            var unitsByLocation = allUnits.stream()
                                          .collect(groupingBy(unit -> Pair.of(unit.getOrt(), unit.getPostleitzahl())));
            var clusters = unitsByLocation.values().stream().map(Cluster::new).collect(toSet());
            System.out.println("Number of clusters (including single unit clusters): " + clusters.size());


            clusters = clusters.stream().filter(cluster -> cluster.getSize() > 1).collect(toSet());

            System.out.println("Number of clusters (without single unit clusters): " + clusters.size());

            var clusterNumberByNetPower = clusters.stream()
                                                  .collect(collectingAndThen(groupingBy(c -> (c.getPower().intValue() / 100) * 100, Collectors.counting()), TreeMap::new));

            var avgClusterActivePower = clusters.stream()
                                                .map(Cluster::getPower)
                                                .mapToDouble(Double::doubleValue)
                                                .average()
                                                .getAsDouble();
            System.out.println("Clusters by power: " + clusterNumberByNetPower);
            System.out.println("Average cluster power: " + avgClusterActivePower);
            System.out.println("Total power: " + (int) allUnits.stream().map(SolarUnit::getNettonennleistung).mapToDouble(Float::doubleValue).sum());

            writeToCSV(clusterNumberByNetPower);

            jaxbObjectToXML(clusters);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    private static void writeToCSV(TreeMap<Integer, Long> clusterNumberByNetPower) {
        String eol = System.getProperty("line.separator");

        try (Writer writer = new FileWriter("clustersByPower.csv")) {
            writer.append("power;number of units").append(eol);
            for (Map.Entry<Integer, Long> entry : clusterNumberByNetPower.entrySet()) {
                writer.append(entry.getKey().toString())
                      .append(';')
                      .append(entry.getValue().toString())
                      .append(eol);
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    private static void jaxbObjectToXML(Set<Cluster> clusters)
    {
        try
        {
            //Create JAXB Context
            JAXBContext jaxbContext = JAXBContext.newInstance(Cluster.class, Clusters.class);

            //Create Marshaller
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            //Required formatting??
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            //Store XML to File
            File file = new File("clusters.xml");

            jaxbMarshaller.marshal(new Clusters(clusters), file);

        }
        catch (JAXBException e)
        {
            e.printStackTrace();
        }
    }
}
