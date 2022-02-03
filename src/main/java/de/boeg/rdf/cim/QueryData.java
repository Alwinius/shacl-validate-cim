package de.boeg.rdf.cim;

import de.boeg.rdf.cim.registers.ExampleDataRegister;
import de.boeg.rdf.cim.typed.RDFS2DatatypeMapGenerator;
import de.boeg.rdf.cim.typed.TypedStreamRDF;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.graph.GraphFactory;

import java.io.IOException;
import java.io.InputStream;

public class QueryData {

    public static void main(String[] args) throws IOException {
        var underTest = ExampleDataRegister.MINIGRID_RD_EQ;

        // setup type mapping
        var typeMap = RDFS2DatatypeMapGenerator.parseDatatypeMap(underTest.rdfs.path);


        // load test data
        var dataGraph = GraphFactory.createDefaultGraph();
        try (InputStream is = ClassLoader.getSystemResourceAsStream(underTest.path)) { // open input stream
            var sink = new TypedStreamRDF(dataGraph, typeMap);
            RDFDataMgr.parse(sink, is, "", Lang.RDFXML);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
