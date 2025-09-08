package com.giga.nexas.dto.bhe.spm.hitbox;

import com.giga.nexas.dto.bhe.spm.Spm;
import com.giga.nexas.io.BinaryReader;
import lombok.Data;

import java.io.IOException;

@Data
public class CSphere extends Spm.SPMHitArea {

    private Integer int1;
    private Integer int2;
    private Integer int3;
    private Integer int4;

    @Override
    public void readInfo(BinaryReader reader) throws IOException {
        int1 = reader.readInt();
        int2 = reader.readInt();
        int3 = reader.readInt();
        int4 = reader.readInt();
    }
}
