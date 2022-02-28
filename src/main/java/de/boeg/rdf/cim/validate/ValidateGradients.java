package de.boeg.rdf.cim.validate;

import de.boeg.rdf.cim.ControllableResource;
import de.boeg.rdf.cim.registers.ExampleDataRegister;
import de.boeg.rdf.cim.registers.RDFSRegister;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Graph;
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

import static de.boeg.rdf.cim.registers.ExampleDataRegister.*;

@Log
public class ValidateGradients {

    private static final String URI_BASE = "https://example.com/#";
    public static final String USE_CASE = "gradients";

    public static void main(String[] args) throws IOException {
        Map<RDFSRegister, Set<String>> graphNamesPerProfile = new EnumMap<>(RDFSRegister.class);

        // setup type mapping
        var typeMap = Util.generateTypeMap(MINIGRID_RD_EQ.rdfs.path, MINIGRID_RD_PD.rdfs.path);

        var dataSet = DatasetFactory.create();
        addData(dataSet, MINIGRID_RD_EQ, typeMap, graphNamesPerProfile);
        addData(dataSet, MINIGRID_RD_PD, typeMap, graphNamesPerProfile);
        addData(dataSet, MINIGRID_RD_PD2, typeMap, graphNamesPerProfile);

        var controllableResources = queryTimeSeries(dataSet, graphNamesPerProfile);

        validateGradients(controllableResources);
    }

    private static void addData(Dataset dataset, ExampleDataRegister register, Map<Node, XSDDatatype> typeMap, Map<RDFSRegister, Set<String>> graphNamesPerProfile) {
        var dataGraph = GraphFactory.createDefaultGraph();
        Util.importFile(register.getPath(USE_CASE), dataGraph, typeMap);

        var graphName = URI_BASE + register.rdfs.name();
        if (register.rdfs.equals(RDFSRegister.RD_PD)) {
            var scenarioTime = getScenarioTime(dataGraph);
            graphName = graphName + "/" + scenarioTime;
        }

        var graphNamesForProfile = graphNamesPerProfile.computeIfAbsent(register.rdfs, t -> new HashSet<>());
        graphNamesForProfile.add(graphName);
        dataset.addNamedModel(graphName, ModelFactory.createModelForGraph(dataGraph));
        dataset.commit();
    }

    @SneakyThrows
    private static Instant getScenarioTime(Graph graph) {
        var queryStr = new String(ClassLoader.getSystemResourceAsStream("generation/scenario-time.sparql").readAllBytes());
        var query = QueryFactory.create(queryStr);
        try (var execution = QueryExecutionFactory.create(query, ModelFactory.createModelForGraph(graph))) {
            var resultSet = execution.execSelect();
            if (!resultSet.hasNext()) {
                throw new IllegalArgumentException("Graph header does not contain scenario time");
            }
            return Instant.parse(resultSet.next().getLiteral("scenarioTime").getString() + "Z");
        }
    }

    private static Collection<ControllableResource> queryTimeSeries(Dataset dataset, Map<RDFSRegister, Set<String>> graphRegister) throws IOException {
        var query = prepareQuery(graphRegister);
        Map<String, ControllableResource> controllableResourceById = new HashMap<>();
        try (var execution = QueryExecutionFactory.create(query, dataset)) {
            var resultSet = execution.execSelect();
            while (resultSet != null && resultSet.hasNext()) {
                var typePropertyRow = resultSet.next();
                var id = typePropertyRow.get("cr").asResource().getURI();
                var resource = controllableResourceById.computeIfAbsent(id, x -> mapToNewCR(typePropertyRow));
                var profileTime = Instant.parse(typePropertyRow.getResource("g").getURI().substring(27));
                resource.getProdTimeSeries().put(profileTime, typePropertyRow.getLiteral("sum").getDouble());
            }
        }
        return controllableResourceById.values();
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
        var timeSeries = controllableResource.getProdTimeSeries();
        if (timeSeries.size() < 2) {
            return;
        }
        var orderedInstants = timeSeries.keySet().stream().sorted().collect(Collectors.toList());
        for (int i = 1; i < timeSeries.size(); i++) {
            var previous = orderedInstants.get(i - 1);
            var current = orderedInstants.get(i);
            var timeDifference = Duration.between(previous, current).getSeconds() / 60.0 / 60.0; // in hours
            var previousValue = timeSeries.get(previous); // in MW
            var currentValue = timeSeries.get(current); // in MW
            var valueDifference = currentValue - previousValue;
            if (valueDifference > 0 && controllableResource.getMaxGradientPlus() != null &&
                          valueDifference / timeDifference > controllableResource.getMaxGradientPlus()) {
                log.warning("Gradient from " + previous + " to " + current + " is larger than allowed for " + controllableResource.getName());
            }

            if (valueDifference < 0 && controllableResource.getMaxGradientMinus() != null &&
                          -valueDifference / timeDifference > controllableResource.getMaxGradientMinus()) {
                log.warning("Gradient from " + previous + " to " + current + " is larger than allowed for " + controllableResource.getName());
            }
        }
    }
}
