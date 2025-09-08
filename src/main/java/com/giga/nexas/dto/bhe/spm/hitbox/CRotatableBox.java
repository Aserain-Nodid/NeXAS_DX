package com.giga.nexas.dto.bhe.spm.hitbox;

import com.giga.nexas.dto.bhe.spm.Spm;
import com.giga.nexas.io.BinaryReader;
import lombok.Data;

import java.io.IOException;

@Data
public class CRotatableBox extends Spm.SPMHitArea {

    // File::ReadInt(a1, (a2 + 4), 8u)
    private Integer int1;
    private Integer int2;

    // File::ReadInt(a1, (a2 + 24), 4u)
    private Integer int3;

    // File::ReadInt(a1, (a2 + 12), 8u)
    private Integer int4;
    private Integer int5;

    // File::ReadInt(a1, (a2 + 28), 4u)
    private Integer int6;

    private Integer int7;
    private Integer int8;
    // *(a2 + 32) = LOWORD(v3[0]);
    private Integer attrU16;

    @Override
    public void readInfo(BinaryReader reader) throws IOException {
        // 2 * int
        int1 = reader.readInt();
        int2 = reader.readInt();

        // 1 * int
        int3 = reader.readInt();

        // 2 * int
        int4 = reader.readInt();
        int5 = reader.readInt();

        // 1 * int
        int6 = reader.readInt();

        // 最后一次只取低 16 位
        int7 = reader.readInt();
        int8 = reader.readInt();

        int raw = reader.readInt();
        attrU16 = raw & 0xFFFF;
    }
}
