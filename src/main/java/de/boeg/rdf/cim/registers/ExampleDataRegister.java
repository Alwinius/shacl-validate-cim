package de.boeg.rdf.cim.registers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ExampleDataRegister {
    MINIGRID_DL("data/20181204T0000Z_T1_AAA_DL_300.xml", RDFSRegister.DL),
    MINIGRID_EQ("data/20181204T0000Z_T1_AAA_EQ_300.xml", RDFSRegister.EQ),
    MINIGRID_RD_EQ("data/RD_EQ_Example.xml", RDFSRegister.RD_EQ),
    MINIGRID_RD_EQ_G("data/RD_EQ_G_Example.xml", RDFSRegister.RD_EQ_G),
    MINIGRID_SSH("data/20181204T0000Z_T1_AAA_SSH_300.xml", RDFSRegister.SSH),
    MINIGRID_TP("data/20181204T0000Z_T1_AAA_TP_300.xml", RDFSRegister.DL);

    public final String path;
    public final RDFSRegister rdfs;
}
