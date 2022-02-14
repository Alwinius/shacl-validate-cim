package de.boeg.rdf.cim.validate;

import de.boeg.rdf.cim.ControllableResource;
import de.boeg.rdf.cim.registers.ExampleDataRegister;
import de.boeg.rdf.cim.registers.RDFSRegister;
import de.boeg.rdf.cim.typed.RDFS2DatatypeMapGenerator;
import lombok.extern.java.Log;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.graph.GraphFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Log
public class ValidateGradients {

    private static final String URI_BASE = "https://example.com/#";

    public static void main(String[] args) throws IOException {
        var rdEqG = ExampleDataRegister.MINIGRID_RD_EQ_G;
        var rdEq = ExampleDataRegister.MINIGRID_RD_EQ;
        var rdPd = ExampleDataRegister.MINIGRID_RD_PD;
        Map<RDFSRegister, Set<String>> graphRegister = new EnumMap<>(RDFSRegister.class);

        // setup type mapping
        var typeMap = RDFS2DatatypeMapGenerator.parseDatatypeMap(rdEqG.rdfs.path);
        typeMap.putAll(RDFS2DatatypeMapGenerator.parseDatatypeMap(rdEq.rdfs.path));
        typeMap.putAll(RDFS2DatatypeMapGenerator.parseDatatypeMap(rdPd.rdfs.path));

        var dataSet = DatasetFactory.create();
        addData(dataSet, rdEq, typeMap, URI_BASE + "RD_EQ", graphRegister, null);
        addData(dataSet, rdEqG, typeMap, URI_BASE + "RD_EQ_G", graphRegister, null);
        addData(dataSet, rdPd, typeMap, URI_BASE + "RD_PD/" + Instant.now(), graphRegister, null);
        addData(dataSet, rdPd, typeMap, URI_BASE + "RD_PD/" + Instant.now().plusSeconds(900), graphRegister, "data/RD_PD_Example2.xml");

        var controllableResources = queryTimeSeries(dataSet, graphRegister);

        validateGradients(controllableResources);
    }

    private static void addData(Dataset dataset, ExampleDataRegister register, Map<Node, XSDDatatype> typeMap, String graphName, Map<RDFSRegister, Set<String>> graphRegister, String pathOverride) {
        var dataGraph = GraphFactory.createDefaultGraph();
        if (pathOverride != null) {
            ShaclValidationExample.importFile(pathOverride, dataGraph, typeMap);
        } else {
            ShaclValidationExample.importFile(register.path, dataGraph, typeMap);
        }
        var graphsOfRdfs = graphRegister.computeIfAbsent(register.rdfs, t -> new HashSet<>());
        graphsOfRdfs.add(graphName);
        dataset.addNamedModel(graphName, ModelFactory.createModelForGraph(dataGraph));
        dataset.commit();
    }

    private static Collection<ControllableResource> queryTimeSeries(Dataset dataset, Map<RDFSRegister, Set<String>> graphRegister) throws IOException {
        var query = prepareQuery(graphRegister);
        ResultSet resultSet;
        Map<String, ControllableResource> controllableResourceMap = new HashMap<>();
        try (var execution = QueryExecutionFactory.create(query, dataset)) {
            resultSet = execution.execSelect();
            while (resultSet != null && resultSet.hasNext()) {
                var typePropertyRow = resultSet.next();
                var id = typePropertyRow.get("cr").asResource().getURI();
                var resource = controllableResourceMap.computeIfAbsent(id, x -> mapToNewCR(typePropertyRow));
                var profileTime = Instant.parse(typePropertyRow.getResource("g").getURI().substring(27));
                resource.getProdTimeSeries().put(profileTime, typePropertyRow.getLiteral("sum").getDouble());
            }
        }
        return controllableResourceMap.values();
    }

    private static Query prepareQuery(Map<RDFSRegister, Set<String>> graphRegister) throws IOException {
        var queryStr = new String(ClassLoader.getSystemResourceAsStream("generation/queryTS.sparql").readAllBytes());
        var planningDataGraphNames = graphRegister.get(RDFSRegister.RD_PD);
        var fromPlanningData = new StringBuilder();
        planningDataGraphNames.forEach(graph -> fromPlanningData.append("FROM NAMED <").append(graph).append(">\n"));
        return QueryFactory.create(queryStr.replace("<RD_PD>", fromPlanningData.toString()));
    }

    private static ControllableResource mapToNewCR(QuerySolution qs) {
        var id = qs.get("cr").asResource().getURI();
        var name = qs.getLiteral("name").getString();
        var maxGradientPlus = Optional.ofNullable(qs.getLiteral("gradientPlus")).map(Literal::getDouble).orElse(null);
        var maxGradientMinus = Optional.ofNullable(qs.getLiteral("gradientMinus")).map(Literal::getDouble).orElse(null);
        return new ControllableResource(id, name, new HashMap<>(), maxGradientPlus, maxGradientMinus);
    }

    private static void validateGradients(Collection<ControllableResource> controllableResources) {
        controllableResources.forEach(ValidateGradients::validateGradient);
    }

    private static void validateGradient(ControllableResource controllableResource) {
        var timeseries = controllableResource.getProdTimeSeries();
        var orderedInstants = timeseries.keySet().stream().sorted().collect(Collectors.toList());
        for (int i = 1; i < timeseries.size(); i++) {
            var previous = orderedInstants.get(i - 1);
            var current = orderedInstants.get(i);
            var timeDifference = Duration.between(previous, current).getSeconds() / 60.0 / 60.0; // in hours
            var previousValue = timeseries.get(previous); // in MW
            var currentValue = timeseries.get(current); // in MW
            var valueDifference = currentValue - previousValue;
            if (valueDifference > 0 && controllableResource.getMaxGradientPlus() != null &&
                          valueDifference / timeDifference > controllableResource.getMaxGradientPlus()) {
                log.warning("Gradient from " + previous + " to " + current + " is larger than allowed for "+controllableResource.getName());
            }

            if (valueDifference < 0 && controllableResource.getMaxGradientMinus() != null &&
                          -valueDifference / timeDifference > controllableResource.getMaxGradientMinus()) {
                log.warning("Gradient from " + previous + " to " + current + " is larger than allowed for "+controllableResource.getName());
            }
        }
    }
}
