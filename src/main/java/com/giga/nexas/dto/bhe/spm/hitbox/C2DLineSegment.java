package com.giga.nexas.dto.bhe.spm.hitbox;

import com.giga.nexas.dto.bhe.spm.Spm;
import com.giga.nexas.io.BinaryReader;
import lombok.Data;

import java.io.IOException;

@Data
public class C2DLineSegment extends Spm.SPMHitArea {

    // x1
    private Integer int1;
    // y1
    private Integer int2;

    // x2
    private Integer int3;
    // y2
    private Integer int4;

    private byte[] skippedBytes;

    @Override
    public void readInfo(BinaryReader reader) throws IOException {
        // 起点
        int1 = reader.readInt();
        int2 = reader.readInt();

        // 终点
        int3 = reader.readInt();
        int4 = reader.readInt();

        skippedBytes = reader.readBytes(8);
    }
}
