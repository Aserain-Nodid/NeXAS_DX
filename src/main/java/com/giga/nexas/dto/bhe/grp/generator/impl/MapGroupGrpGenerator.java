package com.giga.nexas.dto.bhe.grp.generator.impl;

import com.giga.nexas.dto.bhe.grp.generator.GrpFileGenerator;
import com.giga.nexas.dto.bhe.grp.Grp;
import com.giga.nexas.dto.bhe.grp.groupmap.MapGroupGrp;
import com.giga.nexas.io.BinaryWriter;

import java.io.IOException;
import java.util.List;

public class MapGroupGrpGenerator implements GrpFileGenerator<Grp> {

    @Override
    public String getGeneratorKey() {
        return "mapgroup";
    }

    @Override
    public void generate(BinaryWriter writer, Grp grp) {
        MapGroupGrp obj = (MapGroupGrp) grp;
        try {
            writer.writeInt(obj.getGroupList().size());
            for (MapGroupGrp.MapGroup group : obj.getGroupList()) {
                writer.writeInt(group.getExistFlag());
                if (group.getExistFlag() != 0) {
                    writer.writeNullTerminatedString(nullToEmpty(group.getGroupName()));
                    writer.writeNullTerminatedString(nullToEmpty(group.getGroupCodeName()));
                    writer.writeNullTerminatedString(nullToEmpty(group.getGroupResourceName()));
                    writer.writeInt(group.getInt1());

                    writer.writeInt(group.getItems().size());
                    for (MapGroupGrp.Item item : group.getItems()) {
                        writer.writeInt(item.getInt1());
                        writer.writeInt(item.getInt2());
                        writer.writeInt(item.getInt3());
                        writer.writeInt(item.getInt4());
                        writer.writeInt(item.getInt5());
                    }

                    writePairArraySegment(writer, group.getPairArray1());
                    writeIntArraySegment(writer, group.getArray2());
                    writeIntArraySegment(writer, group.getArray3());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate MapGroup.grp", e);
        }
    }

    private void writePairArraySegment(BinaryWriter writer, List<MapGroupGrp.PairArray> pair) throws IOException {
        if (pair == null) {
            writer.writeInt(0);
            return;
        }
        writer.writeInt(pair.size());
        for (MapGroupGrp.PairArray arr : pair) {
            List<MapGroupGrp.Pair> values = (arr != null) ? arr.getValues() : null;
            int len = (values != null) ? values.size() : 0;
            writer.writeInt(len);
            if (len > 0) {
                for (MapGroupGrp.Pair p : values) {
                    if (p == null) {
                        writer.writeInt(0);
                        writer.writeInt(0);
                    } else {
                        writer.writeInt(p.getInt1());
                        writer.writeInt(p.getInt2());
                    }
                }
            }
        }
    }

    private void writeIntArraySegment(BinaryWriter writer, java.util.List<MapGroupGrp.IntArray> list) throws IOException {
        writer.writeInt(list.size());
        for (MapGroupGrp.IntArray arr : list) {
            writer.writeInt(arr.getValues().size());
            for (Integer v : arr.getValues()) {
                writer.writeInt(v != null ? v : 0);
            }
        }
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
