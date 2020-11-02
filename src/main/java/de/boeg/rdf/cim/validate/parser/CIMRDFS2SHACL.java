package de.boeg.rdf.cim.validate.parser;

import lombok.experimental.UtilityClass;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@UtilityClass
public class CIMRDFS2SHACL {

    public static final String SUMMARY_FILE = "generation/schemaSummary.sparql";

    public Shapes generate(String rdfsFile, Map<Node, XSDDatatype> typeMap) throws IOException {

        var rdfsGraph = GraphFactory.createDefaultGraph();

        RDFDataMgr.read(rdfsGraph, ClassLoader.getSystemResourceAsStream(rdfsFile), Lang.RDFXML);

        //get all available rdfs typenames from the model wrapping the given graph
        var typenames = getAllTypenamesFromModel(rdfsGraph);

        //create empty jena shape graph
        var shapeGraph = GraphFactory.createDefaultGraph();
        shapeGraph.getPrefixMapping().setNsPrefix("sh", SHACL.getURI());

        // create shape property triple for all queried typenames
        for (Node type : typenames) {
            var shapeName = NodeFactory.createURI(String.format("%s_Shape", type.getURI()));
            generateNodeShape(shapeName, type).forEach(shapeGraph::add);
            generatePropertyShapes(shapeName, type, rdfsGraph, typeMap).forEach(shapeGraph::add);
        }

        return Shapes.parse(shapeGraph);
    }

    private List<Triple> generatePropertyShapes(Node shapeNode, Node targetClass, Graph rdfsGraph, Map<Node, XSDDatatype> typeMap) throws IOException {
        var shapeTriples = new ArrayList<Triple>();
        var queryStr = new String(ClassLoader.getSystemResourceAsStream(SUMMARY_FILE).readAllBytes());
        var query = QueryFactory.create(queryStr.replace("?parameter", "<".concat(targetClass.getURI()).concat(">")));
        ResultSet resultSet;
        try (var execution = QueryExecutionFactory.create(query, ModelFactory.createModelForGraph(rdfsGraph))) {
            resultSet = execution.execSelect();
            while (resultSet != null && resultSet.hasNext()) {
                var typePropertyRow = resultSet.next();
                var typeProperty = typePropertyRow.get("property").asNode();
                var propertyDataType = typePropertyRow.get("dataType");
                var propertyRange = typePropertyRow.get("range");
                var propertyMultiplicity = typePropertyRow.get("multiplicity").asResource();

                var propertyUri = NodeFactory.createBlankNode();
                shapeTriples.add(new Triple(shapeNode, SHACL.property, propertyUri));
                shapeTriples.add(new Triple(propertyUri, SHACL.path, typeProperty));

                if (propertyDataType != null) {
                    var xsdDataType = typeMap.getOrDefault(typeProperty, XSDDatatype.XSDstring);
                    shapeTriples.add(new Triple(propertyUri, SHACL.datatype, NodeFactory.createURI(xsdDataType.getURI())));
                } else if (propertyRange != null) {
                    shapeTriples.add(new Triple(propertyUri, SHACL.nodeKind, SHACL.IRI));
                }

                shapeTriples.addAll(getMultiplicityTriple(propertyUri, propertyMultiplicity));
            }
        }

        return shapeTriples;
    }

    private List<Triple> generateNodeShape(Node shapeNode, Node targetClass) {
        var shapeTriples = new ArrayList<Triple>();
        shapeTriples.add(new Triple(shapeNode, RDF.type.asNode(), SHACL.NodeShape));
        shapeTriples.add(new Triple(shapeNode, SHACL.closed, NodeFactory.createLiteral("false", XSDDatatype.XSDboolean)));

        var tmp = NodeFactory.createBlankNode();
        shapeTriples.add(new Triple(shapeNode, SHACL.ignoredProperties, tmp));
        shapeTriples.add(new Triple(tmp, RDF.first.asNode(), RDF.type.asNode()));
        shapeTriples.add(new Triple(tmp, RDF.rest.asNode(), RDF.nil.asNode()));

        shapeTriples.add(new Triple(shapeNode, SHACL.targetClass, targetClass));

        return shapeTriples;
    }

    private List<Triple> getMultiplicityTriple(Node uri, Resource multiplicity) {
        var mult = multiplicity.getURI().split("#M:")[1];

        switch (mult) {
            case "0..1":
                return List.of(new Triple(uri, SHACL.maxCount, NodeFactory.createLiteral("1", XSDDatatype.XSDinteger)));
            case "1..1":
                return List.of(new Triple(uri, SHACL.minCount, NodeFactory.createLiteral("1", XSDDatatype.XSDinteger)),
                new Triple(uri, SHACL.maxCount, NodeFactory.createLiteral("1", XSDDatatype.XSDinteger)));
            case "1..n":
                return List.of(new Triple(uri, SHACL.minCount, NodeFactory.createLiteral("1", XSDDatatype.XSDinteger)));
            default:
                return List.of();
        }
    }

    private Set<Node> getAllTypenamesFromModel(Graph rdfsGraph) {
        return rdfsGraph.find(Node.ANY, RDF.type.asNode(), RDFS.Class.asNode()) // get triples with predicate rdf:type
                        .mapWith(Triple::getMatchSubject) // extract class uri
                        .toSet(); // distinct
    }
}
