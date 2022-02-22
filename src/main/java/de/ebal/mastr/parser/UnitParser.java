package de.ebal.mastr.parser;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.apache.commons.lang3.tuple.Triple;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
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
                File file = new File("C:\\Users\\ebal\\Downloads\\Gesamtdatenexport_20220216__4fb071fd20ce4536972c096279e20424\\EinheitenSolar_" + i + ".xml");

                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

                SolarUnits o = (SolarUnits) jaxbUnmarshaller.unmarshal(file);
                var filteredUnits = o.getSolarUnits().stream().filter(unit -> (unit.getFernsteuerbarkeit() != null && unit.getFernsteuerbarkeit() == 1) || unit.getNettonennleistung() >= 100.000).collect(toSet());
                allUnits.addAll(filteredUnits);
            }

            var unitsByLocation = allUnits.stream()
                                          .collect(groupingBy(unit -> Triple.of(unit.getOrt(), unit.getPostleitzahl(), unit.getHauptausrichtung())));
            System.out.println("Number of clusters: " + unitsByLocation.size());

            unitsByLocation = unitsByLocation.entrySet()
                                             .stream()
                                             .filter(entry -> entry.getValue().size() > 1)
                                             .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            var clusterNumberByNetPower = unitsByLocation.values().stream()
                                                         .map(solarUnits -> solarUnits.stream().map(SolarUnit::getNettonennleistung).reduce(Float::sum))
                                                         .flatMap(Optional::stream)
                                                         .collect(collectingAndThen(groupingBy(f -> (f.intValue() / 30) * 30, Collectors.counting()), TreeMap::new));

            var avgClusterActivePower = unitsByLocation.values().stream()
                                                       .map(solarUnits -> solarUnits.stream().map(SolarUnit::getNettonennleistung).reduce(Float::sum))
                                                       .flatMap(Optional::stream)
                                                       .mapToDouble(Float::doubleValue)
                                                       .average()
                                                       .getAsDouble();

            System.out.println("Number of clusters with more than one entry: " + unitsByLocation.size());
            System.out.println("Total number of considered units: " + allUnits.size());
            System.out.println("Clusters by power: " + clusterNumberByNetPower);
            System.out.println("Average cluster power: " + avgClusterActivePower);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}
