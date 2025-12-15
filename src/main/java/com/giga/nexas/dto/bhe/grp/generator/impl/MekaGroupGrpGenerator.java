package com.giga.nexas.dto.bhe.grp.generator.impl;

import com.giga.nexas.dto.bhe.grp.generator.GrpFileGenerator;
import com.giga.nexas.dto.bhe.grp.Grp;
import com.giga.nexas.dto.bhe.grp.groupmap.MekaGroupGrp;
import com.giga.nexas.io.BinaryWriter;

import java.io.IOException;

public class MekaGroupGrpGenerator implements GrpFileGenerator<Grp> {

    @Override
    public String getGeneratorKey() {
        return "mekagroup";
    }

    @Override
    public void generate(BinaryWriter writer, Grp grp) throws IOException {
        MekaGroupGrp mekaGroupGrp = (MekaGroupGrp) grp;
        writer.writeInt(mekaGroupGrp.getMekaList().size());

        for (MekaGroupGrp.MekaGroup group : mekaGroupGrp.getMekaList()) {
            writer.writeInt(group.getExistFlag());
            if (group.getExistFlag() != 0) {
                writer.writeNullTerminatedString(group.getMekaName());
                writer.writeNullTerminatedString(group.getMekaCodeName());
            }
        }
    }
}
