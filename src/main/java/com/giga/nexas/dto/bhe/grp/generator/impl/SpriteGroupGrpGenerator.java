package com.giga.nexas.dto.bhe.grp.generator.impl;

import com.giga.nexas.dto.bhe.grp.generator.GrpFileGenerator;
import com.giga.nexas.dto.bhe.grp.Grp;
import com.giga.nexas.dto.bhe.grp.groupmap.SpriteGroupGrp;
import com.giga.nexas.io.BinaryWriter;

import java.io.IOException;

public class SpriteGroupGrpGenerator implements GrpFileGenerator<Grp> {

    @Override
    public String getGeneratorKey() {
        return "spritegroup";
    }

    @Override
    public void generate(BinaryWriter writer, Grp grp) throws IOException {
        SpriteGroupGrp spriteGroupGrp = (SpriteGroupGrp) grp;
        writer.writeInt(spriteGroupGrp.getSpriteList().size());

        for (SpriteGroupGrp.SpriteGroupEntry entry : spriteGroupGrp.getSpriteList()) {
            writer.writeInt(entry.getExistFlag());
            if (entry.getExistFlag() != 0) {
                writer.writeNullTerminatedString(entry.getSpriteFileName());
                writer.writeNullTerminatedString(entry.getSpriteCodeName());
                writer.writeInt(entry.getParam());
            }
        }
    }
}
