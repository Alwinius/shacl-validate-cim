package de.boeg.rdf.cim.registers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ExampleDataRegister {
    MINIGRID_DL("20181204T0000Z_T1_AAA_DL_300.xml", RDFSRegister.DL),
    MINIGRID_EQ("20181204T0000Z_T1_AAA_EQ_300.xml", RDFSRegister.EQ),
    MINIGRID_RD_EQ("RD_EQ_Example.xml", RDFSRegister.RD_EQ),
    MINIGRID_RD_EQ_G("RD_EQ_G_Example.xml", RDFSRegister.RD_EQ_G),
    MINIGRID_RD_PD("RD_PD_Example.xml", RDFSRegister.RD_PD),
    MINIGRID_RD_PD2("RD_PD_Example2.xml", RDFSRegister.RD_PD),
    MINIGRID_SSH("20181204T0000Z_T1_AAA_SSH_300.xml", RDFSRegister.SSH),
    MINIGRID_TP("20181204T0000Z_T1_AAA_TP_300.xml", RDFSRegister.DL);

    private final String path;
    public final RDFSRegister rdfs;

    public String getPath(String useCase) {
        if (useCase == null) {
            return "data/" + path;
        }
        return "data/" + useCase + "/" + path;
    }
}
