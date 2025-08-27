package com.giga.nexas.dto.bhe.grp.generator.impl;

import com.giga.nexas.dto.bhe.grp.generator.GrpFileGenerator;
import com.giga.nexas.dto.bhe.grp.Grp;
import com.giga.nexas.dto.bhe.grp.groupmap.ProgramMaterialGrp;
import com.giga.nexas.io.BinaryWriter;

import java.io.IOException;

public class ProgramMaterialGrpGenerator implements GrpFileGenerator<Grp> {

    @Override
    public String getGeneratorKey() {
        return "programmaterial";
    }

    @Override
    public void generate(BinaryWriter writer, Grp grp) {
        ProgramMaterialGrp obj = (ProgramMaterialGrp) grp;
        try {
            writeIntArraySegment(writer, obj.getArray1());
            writeIntArraySegment(writer, obj.getArray2());
            writeIntArraySegment(writer, obj.getArray3());
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate ProgramMaterial.grp", e);
        }
    }

    private void writeIntArraySegment(BinaryWriter writer, java.util.List<ProgramMaterialGrp.IntArray> list) throws IOException {
        writer.writeInt(list.size());
        for (ProgramMaterialGrp.IntArray arr : list) {
            writer.writeInt(arr.getValues().size());
            for (Integer v : arr.getValues()) {
                writer.writeInt(v != null ? v : 0);
            }
        }
    }

}
