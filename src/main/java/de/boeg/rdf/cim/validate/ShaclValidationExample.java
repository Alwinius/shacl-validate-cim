package de.boeg.rdf.cim.validate;

import de.boeg.rdf.cim.registers.ExampleDataRegister;
import de.boeg.rdf.cim.typed.RDFS2DatatypeMapGenerator;
import de.boeg.rdf.cim.typed.TypedStreamRDF;
import de.boeg.rdf.cim.validate.parser.CIMRDFS2SHACL;
import de.boeg.rdf.cim.validate.parser.ShaclReader;
import lombok.extern.java.Log;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.validation.ReportEntry;
import org.apache.jena.sparql.graph.GraphFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.*;

@Log
public class ShaclValidationExample {

    private static final RunConfig current = RunConfig.SUMMARY;

    private static final String SUMMARY_TEMPLATE = "##################################################\n"
                                                             + "\t# Validation Result\n"
                                                             + "\t#   data file: %s\n"
                                                             + "\t#   shape size: %d\n"
                                                             + "\t#   model size: %d\n"
                                                             + "\t#   is conform: %s\n"
                                                             + "\t#   violation count: %d\n"
                                                             + "\t#   took: %d ms\n"
                                                             + "\t##################################################\n";

    public static void main(String[] args) throws IOException {
        var underTest = ExampleDataRegister.MINIGRID_RD_EQ;

        // setup type mapping
        var typeMap = RDFS2DatatypeMapGenerator.parseDatatypeMap(underTest.rdfs.path);

        // setup shapes
        var shapesGenerated = CIMRDFS2SHACL.generate(underTest.rdfs.path, typeMap);
        var shapesManual = ShaclReader.readFromFile("rules/RD_EQ.ttl");

        RDFDataMgr.write(new FileOutputStream("withClass.ttl"), ModelFactory.createModelForGraph(shapesGenerated.getGraph()), RDFFormat.TURTLE_PRETTY);
        doBenchmark(typeMap, shapesManual, underTest.path);
        doBenchmark(typeMap, shapesGenerated, underTest.path);
    }

    private static void doBenchmark(Map<Node, XSDDatatype> typeMap, Shapes shapes, String dataFile) {
        // load test data
        var dataGraph = GraphFactory.createDefaultGraph();
        try (InputStream is = ClassLoader.getSystemResourceAsStream(dataFile)) { // open input stream
            var sink = new TypedStreamRDF(dataGraph, typeMap);
            RDFDataMgr.parse(sink, is, "", Lang.RDFXML);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // validate
        long tic = System.currentTimeMillis();
        var report = ShaclValidator.get().validate(shapes, dataGraph);
        long toc = System.currentTimeMillis();

        // result
        if (current.isMeasurement) {
            log.info("file, triple, violations, time");
            log.info(format("%s,%d,%d,%d",
                            dataFile,
                            dataGraph.size(),
                            report.getEntries().size(),
                            toc - tic));
        } else {
            log.info(format(SUMMARY_TEMPLATE,
                            dataFile,
                            shapes.numRootShapes(),
                            dataGraph.size(),
                            report.conforms(),
                            report.getEntries().size(),
                            toc - tic));

            if (current.SHOW_COUNTING) {
                log.info("Counting-Report:");
                printReportCounting(report);
            }

            if (current.SHOW_SUMMARY) {
                RDFDataMgr.write(System.out, report.getModel(), Lang.TTL);
//                log.info("Summary:");
//                printReportSummary(report);
            }
        }
    }

    private static void printReportSummary(ValidationReport report) {
        report.getEntries().stream()
              .map(ReportEntry::toString)
              .forEach(log::warning);
    }

    private static void printReportCounting(ValidationReport report) {
        report.getEntries().stream()
              .collect(Collectors.groupingBy(ReportEntry::constraint))
              .entrySet().stream()
              .map(es -> format("%s\"%s\" count:%d", es.getKey(), es.getValue().get(0).message(), es.getValue().size()))
              .forEach(log::warning);
    }
}
