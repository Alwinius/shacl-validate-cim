package de.boeg.rdf.cim;

import de.boeg.rdf.cim.registers.ExampleDataRegister;
import de.boeg.rdf.cim.typed.RDFS2DatatypeMapGenerator;
import de.boeg.rdf.cim.validate.ShaclValidationExample;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.graph.GraphFactory;

import java.io.IOException;

public class QueryData {

    public static void main(String[] args) throws IOException {
        var rdEqG = ExampleDataRegister.MINIGRID_RD_EQ_G;
        var rdEq = ExampleDataRegister.MINIGRID_RD_EQ;
        var rdPd = ExampleDataRegister.MINIGRID_RD_PD;

        // setup type mapping
        var typeMap = RDFS2DatatypeMapGenerator.parseDatatypeMap(rdEqG.rdfs.path);
        typeMap.putAll(RDFS2DatatypeMapGenerator.parseDatatypeMap(rdEq.rdfs.path));
        typeMap.putAll(RDFS2DatatypeMapGenerator.parseDatatypeMap(rdPd.rdfs.path));


        // load test data
        var dataGraph = GraphFactory.createDefaultGraph();
        ShaclValidationExample.importFile(rdEqG.path, dataGraph, typeMap);
        ShaclValidationExample.importFile(rdEq.path, dataGraph, typeMap);
        ShaclValidationExample.importFile(rdPd.path, dataGraph, typeMap);

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
