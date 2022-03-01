package de.boeg.rdf.cim;

import de.boeg.rdf.cim.registers.ExampleDataRegister;
import de.boeg.rdf.cim.validate.Util;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.graph.GraphFactory;

import java.io.IOException;

public class QueryData {
    public static final String USE_CASE = "planning-data";

    public static void main(String[] args) throws IOException {
        var rdEqG = ExampleDataRegister.MINIGRID_RD_EQ_G;
        var rdEq = ExampleDataRegister.MINIGRID_RD_EQ;
        var rdPd = ExampleDataRegister.MINIGRID_RD_PD;

        // setup type mapping
        var typeMap = Util.generateTypeMap(rdEq.rdfs.path, rdEqG.rdfs.path, rdPd.rdfs.path);

        // load test data
        var dataGraph = GraphFactory.createDefaultGraph();
        Util.importFile(rdEqG.getPath(USE_CASE), dataGraph, typeMap);
        Util.importFile(rdEq.getPath(USE_CASE), dataGraph, typeMap);
        Util.importFile(rdPd.getPath(USE_CASE), dataGraph, typeMap);

        var queryStr = new String(ClassLoader.getSystemResourceAsStream("generation/example-query.sparql").readAllBytes());
        var query = QueryFactory.create(queryStr);
        ResultSet resultSet;
        try (var execution = QueryExecutionFactory.create(query, ModelFactory.createModelForGraph(dataGraph))) {
            resultSet = execution.execSelect();
            while (resultSet != null && resultSet.hasNext()) {
                var typePropertyRow = resultSet.next();
                System.out.println(typePropertyRow.toString());
            }
        }

    }

}
