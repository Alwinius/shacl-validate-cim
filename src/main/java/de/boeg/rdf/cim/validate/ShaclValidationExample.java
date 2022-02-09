package de.boeg.rdf.cim.validate;

import de.boeg.rdf.cim.registers.ExampleDataRegister;
import de.boeg.rdf.cim.typed.RDFS2DatatypeMapGenerator;
import de.boeg.rdf.cim.typed.TypedStreamRDF;
import de.boeg.rdf.cim.validate.parser.CIMRDFS2SHACL;
import de.boeg.rdf.cim.validate.parser.ShaclReader;
import lombok.extern.java.Log;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Graph;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.*;

@Log
public class ShaclValidationExample {

    private static final RunConfig current = RunConfig.SUMMARY;

    private static final String SUMMARY_TEMPLATE = "##################################################\n"
                                                             + "\t# Validation Result\n"
                                                             + "\t#   data files: %s\n"
                                                             + "\t#   shape size: %d\n"
                                                             + "\t#   model size: %d\n"
                                                             + "\t#   is conform: %s\n"
                                                             + "\t#   violation count: %d\n"
                                                             + "\t#   took: %d ms\n"
                                                             + "\t##################################################\n";

    public static void main(String[] args) throws IOException {
        var rdEqG = ExampleDataRegister.MINIGRID_RD_EQ_G;
        var rdEq = ExampleDataRegister.MINIGRID_RD_EQ;
        var rdPd = ExampleDataRegister.MINIGRID_RD_PD;

        // setup type mapping
        var typeMap = RDFS2DatatypeMapGenerator.parseDatatypeMap(rdEqG.rdfs.path);
        typeMap.putAll(RDFS2DatatypeMapGenerator.parseDatatypeMap(rdEq.rdfs.path));
        typeMap.putAll(RDFS2DatatypeMapGenerator.parseDatatypeMap(rdPd.rdfs.path));

        // setup shapes
        var shapesGeneratedRdEqG = CIMRDFS2SHACL.generate(rdEqG.rdfs.path, typeMap);
        var shapesGeneratedRdEq = CIMRDFS2SHACL.generate(rdEq.rdfs.path, typeMap);
        var shapesManualRdEqG = ShaclReader.readFromFile(rdEqG.rdfs.rulesPath);
        var shapesManualRdEq = ShaclReader.readFromFile(rdEq.rdfs.rulesPath);
        var shapesCombined = ShaclReader.readFromFile("rules/cross-profile.ttl");

        RDFDataMgr.write(new FileOutputStream("withClass.ttl"), ModelFactory.createModelForGraph(shapesGeneratedRdEqG.getGraph()), RDFFormat.TURTLE_PRETTY);
        doBenchmark(typeMap, shapesManualRdEqG, List.of(rdEqG.path));
        doBenchmark(typeMap, shapesManualRdEq, List.of(rdEq.path));
        doBenchmark(typeMap, shapesGeneratedRdEqG, List.of(rdEqG.path));
        doBenchmark(typeMap, shapesGeneratedRdEq, List.of(rdEq.path));
        doBenchmark(typeMap, shapesCombined, List.of(rdEqG.path, rdEq.path, rdPd.path));
    }

    public static void importFile(String dataFile1, Graph dataGraph, Map<Node, XSDDatatype> typeMap) {
        try (InputStream is = ClassLoader.getSystemResourceAsStream(dataFile1)) { // open input stream
            var sink = new TypedStreamRDF(dataGraph, typeMap);
            RDFDataMgr.parse(sink, is, "", Lang.RDFXML);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void doBenchmark(Map<Node, XSDDatatype> typeMap, Shapes shapes, List<String> dataFiles) {
        var dataGraph = GraphFactory.createDefaultGraph();
        dataFiles.forEach(dataFile -> importFile(dataFile, dataGraph, typeMap));

        // validate
        long tic = System.currentTimeMillis();
        var report = ShaclValidator.get().validate(shapes, dataGraph);
        long toc = System.currentTimeMillis();

        // result
        if (current.isMeasurement) {
            log.info("files, triple, violations, time");
            log.info(format("%s,%d,%d,%d",
                            dataFiles,
                            dataGraph.size(),
                            report.getEntries().size(),
                            toc - tic));
        } else {
            log.info(format(SUMMARY_TEMPLATE,
                            dataFiles,
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
            }
        }
    }

    private static void printReportCounting(ValidationReport report) {
        report.getEntries().stream()
              .collect(Collectors.groupingBy(ReportEntry::constraint))
              .entrySet().stream()
              .map(es -> format("%s\"%s\" count:%d", es.getKey(), es.getValue().get(0).message(), es.getValue().size()))
              .forEach(log::info);
    }
}
