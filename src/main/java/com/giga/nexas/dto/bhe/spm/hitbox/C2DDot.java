package com.giga.nexas.dto.bhe.spm.hitbox;

import com.giga.nexas.dto.bhe.spm.Spm;
import com.giga.nexas.io.BinaryReader;
import lombok.Data;

import java.io.IOException;

@Data
public class C2DDot extends Spm.SPMHitArea {

    // x
    private Integer int1;
    // y
    private Integer int2;

    private byte[] skippedBytes;

    @Override
    public void readInfo(BinaryReader reader) throws IOException {
        int1 = reader.readInt();
        int2 = reader.readInt();

        skippedBytes = reader.readBytes(16);
    }
}
