package de.boeg.rdf.cim.typed;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This implementation of an StreamRDF reads triple and uses a map too type them with an XSDDatatype.
 * The result will be a dataset with all found graphs.
 * The Stream should be used with the RDFDataMgr.
 *
 * @author Merlin Bögershausen merlin.boegershausen@rwth-aachen.de
 * @see XSDDatatype
 * @see org.apache.jena.riot.RDFDataMgr
 */
public class TypedStreamRDF implements StreamRDF {

    private final Map<Node, XSDDatatype> datatypeMapping = new HashMap<>();
    private final Map<String, String> prefixMapping = new HashMap<>();
    private final Graph targetGraph;
    private String base = "";

    /**
     * Configure the Stream with the applied datatype map
     *
     * @param datatypeMapping rdf:Property to XSDDatatype
     */
    public TypedStreamRDF(Graph targetGraph, Map<Node, XSDDatatype> datatypeMapping) {
        this.datatypeMapping.putAll(datatypeMapping);
        this.targetGraph = targetGraph;
    }

    /**
     * Adds the empty default graph to the sink
     */
    public void start() {
        Objects.requireNonNull(targetGraph);
    }

    /**
     * Reader found a triple, we type and add it to the default graph
     *
     * @param triple new triple
     */
    public void triple(Triple triple) {
        var typedTriple = typeTriple(triple);
        this.targetGraph.add(typedTriple);
    }

    /**
     * Reader found a triple, we type and add it to the respective graph
     *
     * @param quad new quad (triple with graph)
     */
    public void quad(Quad quad) {
        var typedTriple = typeTriple(quad.asTriple());
        this.targetGraph.add(typedTriple);
    }

    /**
     * Read found the base of our dataset
     *
     * @param base base value
     */
    public void base(String base) {
        this.base = base;
    }

    /**
     * Reader found prefix mappings for our dataset
     *
     * @param prefix new placeholder
     * @param iri    iti to replace
     */
    public void prefix(String prefix, String iri) {
        this.prefixMapping.put(prefix, iri);
    }

    /**
     * Reader finished reading, we assemble the dataset from the found graphs
     */
    public void finish() {
    }

    /**
     * If the triple contains a literal we extract that and apply the typing, otherwise the original triple is returned
     *
     * @param triple triple to type
     *
     * @return typed literal or original
     */
    private Triple typeTriple(Triple triple) {
        if (triple.getMatchObject().isLiteral()) {// only apply typing of object is literal
            /* determine datatype, use xsd:string as default */
            var datatype = datatypeMapping.getOrDefault(triple.getPredicate(), XSDDatatype.XSDstring);
            /* generate typed literal */
            var literalValue = triple.getObject().getLiteralLexicalForm();
            var typedLiteral = ResourceFactory.createTypedLiteral(literalValue, datatype);
            /* generate new triple */
            triple = new Triple(triple.getSubject(), triple.getPredicate(), typedLiteral.asNode());
        }
        return triple;
    }
}