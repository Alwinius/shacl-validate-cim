package de.boeg.rdf.cim.validate.parser;

import lombok.experimental.UtilityClass;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.Shapes;

@UtilityClass
public class ShaclReader {

    public static Shapes readFromFile(String path) {

        Graph shapesGraph = RDFDataMgr.loadGraph(path);
        return Shapes.parse(shapesGraph);
    }

}
