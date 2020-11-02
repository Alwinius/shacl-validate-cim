package de.boeg.rdf.cim.typed;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.graph.GraphFactory;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class RDFS2DatatypeMapGenerator {

    public final String TYPES_FILE = "generation/propertyTypes.sparql";
    private final String CIM_PREFIX = "http://iec.ch/TC57/2013/CIM-schema-cim16#";

    private final Map<String, XSDDatatype> cimsToXsd = Map.of(
              "Decimal", XSDDatatype.XSDdecimal,
              "String", XSDDatatype.XSDstring,
              "Integer", XSDDatatype.XSDinteger,
              "Boolean", XSDDatatype.XSDboolean,
              "MonthDay", XSDDatatype.XSDgMonthDay,
              "DateTime", XSDDatatype.XSDdateTime,
              "Date", XSDDatatype.XSDdate,
              "Float", XSDDatatype.XSDdouble);

    @SneakyThrows
    public Map<Node, XSDDatatype> parseDatatypeMap(String rdfsFile) {
        var rdfsGraph = GraphFactory.createDefaultGraph();
        var is = ClassLoader.getSystemResourceAsStream(rdfsFile);
        RDFDataMgr.read(rdfsGraph, is, Lang.RDFXML);
        return parseDatatypeMap(rdfsGraph);
    }

    @SneakyThrows
    public Map<Node, XSDDatatype> parseDatatypeMap(Graph rdfsGraph) {
        var queryString = new String(ClassLoader.getSystemResourceAsStream(TYPES_FILE).readAllBytes());
        var query = QueryFactory.create(queryString);
        var model = ModelFactory.createModelForGraph(rdfsGraph);
        var resultMap = new HashMap<Node, XSDDatatype>();

        try (var execution = QueryExecutionFactory.create(query, model)) {
            var resultSet = execution.execSelect();
            while (resultSet.hasNext()) {
                var solution = resultSet.nextSolution();

                var propertyName = solution.getLiteral("propertyName").getString();
                var propertyURI = CIM_PREFIX + propertyName;
                var cimsType = solution.get("cimsPrimitive").toString();

                var xsdType = cimsToXsd.getOrDefault(cimsType, XSDDatatype.XSDstring);
                resultMap.put(NodeFactory.createURI(propertyURI), xsdType);
            }
        }

        return resultMap;
    }
}
