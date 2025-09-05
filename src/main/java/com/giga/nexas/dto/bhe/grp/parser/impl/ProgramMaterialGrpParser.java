package com.giga.nexas.dto.bhe.grp.parser.impl;

import com.giga.nexas.dto.bhe.grp.Grp;
import com.giga.nexas.dto.bhe.grp.groupmap.ProgramMaterialGrp;
import com.giga.nexas.dto.bhe.grp.parser.GrpFileParser;
import com.giga.nexas.io.BinaryReader;

import java.util.ArrayList;
import java.util.List;

public class ProgramMaterialGrpParser implements GrpFileParser<Grp> {

    @Override
    public String getParserKey() {
        return "programmaterial";
    }

    @Override
    public Grp parse(BinaryReader reader) {
        // 创建结果对象
        ProgramMaterialGrp result = new ProgramMaterialGrp();

        // diff head
        int headerCount = reader.readInt();
        List<ProgramMaterialGrp.HeaderItem> header = new ArrayList<>(Math.max(headerCount, 0));
        for (int i = 0; i < headerCount; i++) {
            ProgramMaterialGrp.HeaderItem h = new ProgramMaterialGrp.HeaderItem();
            h.setInt1(reader.readInt());
            h.setInt2(reader.readInt());
            h.setInt3(reader.readInt());
            h.setInt4(reader.readInt());
            header.add(h);
        }
        result.setHeader(header);

        // 读取三段数组的数组
        result.setPairArrary1(readPairArraySegment(reader));
        result.setArray2(readIntArraySegment(reader));
        result.setArray3(readIntArraySegment(reader));

        return result;
    }

    private List<ProgramMaterialGrp.PairArray> readPairArraySegment(BinaryReader reader) {
        int segCount = reader.readInt();
        List<ProgramMaterialGrp.PairArray> list = new ArrayList<>(Math.max(segCount, 0));
        for (int i = 0; i < segCount; i++) {
            int len = reader.readInt();
            ProgramMaterialGrp.PairArray arr = new ProgramMaterialGrp.PairArray();
            List<ProgramMaterialGrp.Pair> values = arr.getValues();
            for (int k = 0; k < len; k++) {
                ProgramMaterialGrp.Pair p = new ProgramMaterialGrp.Pair();
                p.setFirst(reader.readInt());
                p.setSecond(reader.readInt());
                values.add(p);
            }
            list.add(arr);
        }
        return list;
    }

    /**
     * 读取一段“数组的数组”：int32 count -> repeat count: int32 len -> len * int32
     */
    private List<ProgramMaterialGrp.IntArray> readIntArraySegment(BinaryReader reader) {
        int segCount = reader.readInt();
        List<ProgramMaterialGrp.IntArray> list = new ArrayList<>(Math.max(segCount, 0));
        for (int i = 0; i < segCount; i++) {
            int len = reader.readInt();
            ProgramMaterialGrp.IntArray arr = new ProgramMaterialGrp.IntArray();
            List<Integer> values = arr.getValues();
            for (int k = 0; k < len; k++) {
                values.add(reader.readInt());
            }
            list.add(arr);
        }
        return list;
    }
}
