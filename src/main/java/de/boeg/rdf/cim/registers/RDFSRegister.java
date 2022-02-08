package de.boeg.rdf.cim.registers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum RDFSRegister {
    DL("rdfs/DiagramLayoutProfileRDFSAugmented-v2_4_15-16Feb2016.rdf", null),
    DY("rdfs/DynamicsProfileRDFSAugmented_noAbstract-v2_4_15-16Feb2016.rdf", null),
    EQ("rdfs/EquipmentProfileCoreShortCircuitOperationRDFSAugmented-v2_4_15-4Jul2016.rdf", null),
    GL("rdfs/GeographicalLocationProfileRDFSAugmented-v2_4_15-16Feb2016.rdf", null),
    SSH("rdfs/SteadyStateHypothesisProfileRDFSAugmented-v2_4_15-16Feb2016.rdf", null),
    TP("rdfs/TopologyProfileRDFSAugmented-v2_4_15-16Feb2016.rdf", null),
    RD_EQ("rdfs/RD_EQ.rdf", "rules/RD_EQ.ttl"),
    RD_EQ_G("rdfs/RD_EQ_G.rdf", "rules/RD_EQ_G.ttl"),
    RD_PD("rdfs/RD_PD.rdf", null);

    public final String path;
    public final String rulesPath;
}
