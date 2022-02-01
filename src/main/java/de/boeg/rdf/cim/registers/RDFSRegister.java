package de.boeg.rdf.cim.registers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum RDFSRegister {
    DL("rdfs/DiagramLayoutProfileRDFSAugmented-v2_4_15-16Feb2016.rdf"),
    DY("rdfs/DynamicsProfileRDFSAugmented_noAbstract-v2_4_15-16Feb2016.rdf"),
    EQ("rdfs/EquipmentProfileCoreShortCircuitOperationRDFSAugmented-v2_4_15-4Jul2016.rdf"),
    GL("rdfs/GeographicalLocationProfileRDFSAugmented-v2_4_15-16Feb2016.rdf"),
    SSH("rdfs/SteadyStateHypothesisProfileRDFSAugmented-v2_4_15-16Feb2016.rdf"),
    TP("rdfs/TopologyProfileRDFSAugmented-v2_4_15-16Feb2016.rdf"),
    RD_EQ("rdfs/RD_EQ.rdf");

    public final String path;
}
