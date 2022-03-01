package de.boeg.rdf.cim.validate;

import de.boeg.rdf.cim.typed.RDFS2DatatypeMapGenerator;
import de.boeg.rdf.cim.typed.TypedStreamRDF;
import lombok.experimental.UtilityClass;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class Util {

    public static void importFile(String dataFile, Graph dataGraph, Map<Node, XSDDatatype> typeMap) {
        var sink = new TypedStreamRDF(dataGraph, typeMap);
        RDFParser.source(dataFile).base("").lang(Lang.RDFXML).parse(sink);
    }

    public static Map<Node, XSDDatatype> generateTypeMap(String... rdfsPaths) {
        Map<Node, XSDDatatype> typeMap = new HashMap<>();
        Arrays.stream(rdfsPaths)
              .forEach(path -> typeMap.putAll(RDFS2DatatypeMapGenerator.parseDatatypeMap(path)));
        return typeMap;
    }
}
