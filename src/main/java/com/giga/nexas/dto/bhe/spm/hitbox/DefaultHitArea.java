package com.giga.nexas.dto.bhe.spm.hitbox;

import com.giga.nexas.dto.bhe.spm.Spm;
import com.giga.nexas.io.BinaryReader;
import lombok.Data;

import java.io.IOException;

@Data
public class DefaultHitArea extends Spm.SPMHitArea {

    private Integer xMin;
    private Integer xMax;
    private Integer yMin;
    private Integer yMax;
    private Integer zMin;
    private Integer zMax;

    @Override
    public void readInfo(BinaryReader reader) throws IOException {
        xMin = reader.readInt();
        xMax = reader.readInt();
        yMin = reader.readInt();
        yMax = reader.readInt();
        zMin = reader.readInt();
        zMax = reader.readInt();
    }
}
