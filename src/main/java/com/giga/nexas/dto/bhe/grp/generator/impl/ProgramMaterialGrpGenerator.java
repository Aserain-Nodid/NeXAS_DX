package com.giga.nexas.dto.bhe.grp.generator.impl;

import com.giga.nexas.dto.bhe.grp.Grp;
import com.giga.nexas.dto.bhe.grp.generator.GrpFileGenerator;
import com.giga.nexas.dto.bhe.grp.groupmap.ProgramMaterialGrp;
import com.giga.nexas.io.BinaryWriter;

import java.io.IOException;
import java.util.List;

public class ProgramMaterialGrpGenerator implements GrpFileGenerator<Grp> {

    @Override
    public String getGeneratorKey() {
        return "programmaterial";
    }

    @Override
    public void generate(BinaryWriter writer, Grp grp) {
        ProgramMaterialGrp obj = (ProgramMaterialGrp) grp;
        try {
            // diff head
            List<ProgramMaterialGrp.HeaderItem> header = obj.getHeader();
            writer.writeInt(header.size());
            for (ProgramMaterialGrp.HeaderItem h : header) {
                writer.writeInt(h != null ? h.getInt1() : 0);
                writer.writeInt(h != null ? h.getInt2() : 0);
                writer.writeInt(h != null ? h.getInt3() : 0);
                writer.writeInt(h != null ? h.getInt4() : 0);
            }

            writePairArraySegment(writer, obj.getPairArrary1());
            writeIntArraySegment(writer, obj.getArray2());
            writeIntArraySegment(writer, obj.getArray3());
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate ProgramMaterial.grp", e);
        }
    }

    private void writePairArraySegment(BinaryWriter writer, List<ProgramMaterialGrp.PairArray> list) throws IOException {
        writer.writeInt(list.size());
        for (ProgramMaterialGrp.PairArray arr : list) {
            List<ProgramMaterialGrp.Pair> values = (arr != null) ? arr.getValues() : null;
            int len = (values != null) ? values.size() : 0;
            writer.writeInt(len);
            if (len > 0) {
                for (ProgramMaterialGrp.Pair p : values) {
                    if (p == null) {
                        writer.writeInt(0);
                        writer.writeInt(0);
                    } else {
                        writer.writeInt(p.getFirst());
                        writer.writeInt(p.getSecond());
                    }
                }
            }
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
