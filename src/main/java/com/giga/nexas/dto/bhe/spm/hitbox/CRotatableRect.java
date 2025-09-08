package com.giga.nexas.dto.bhe.spm.hitbox;

import com.giga.nexas.dto.bhe.spm.Spm;
import com.giga.nexas.io.BinaryReader;
import lombok.Data;

import java.io.IOException;

@Data
public class CRotatableRect extends Spm.SPMHitArea {

    // centerX
    private Integer int1;
    // centerY
    private Integer int2;

    // width
    private Integer int3;
    // height
    private Integer int4;

    private byte[] skippedBytes1;
    private byte[] skippedBytes2;

    // a2[5] = LOWORD()
    private Integer attrU16;

    @Override
    public void readInfo(BinaryReader reader) throws IOException {
        int1 = reader.readInt();
        int2 = reader.readInt();

        skippedBytes1 = reader.readBytes(4);

        int3 = reader.readInt();
        int4 = reader.readInt();

        skippedBytes2 = reader.readBytes(4);

        int raw = reader.readInt();
        attrU16 = raw & 0xFFFF;
    }
}
