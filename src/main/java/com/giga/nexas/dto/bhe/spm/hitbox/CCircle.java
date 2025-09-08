package com.giga.nexas.dto.bhe.spm.hitbox;

import com.giga.nexas.dto.bhe.spm.Spm;
import com.giga.nexas.io.BinaryReader;
import lombok.Data;

import java.io.IOException;

@Data
public class CCircle extends Spm.SPMHitArea {

    // centerX
    private Integer int1;
    // centerY
    private Integer int2;

    private byte[] skippedBytes;

    // radius
    private Integer int3;

    @Override
    public void readInfo(BinaryReader reader) throws IOException {
        int1 = reader.readInt();
        int2 = reader.readInt();

        skippedBytes = reader.readBytes(8);

        int3 = reader.readInt();
    }
}
