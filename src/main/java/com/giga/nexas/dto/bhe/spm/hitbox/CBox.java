package com.giga.nexas.dto.bhe.spm.hitbox;

import com.giga.nexas.dto.bhe.spm.Spm;
import com.giga.nexas.io.BinaryReader;
import lombok.Data;

import java.io.IOException;

@Data
public class CBox extends Spm.SPMHitArea {

    // 包围盒最小 X/Y/Z。
    private Integer minX;
    private Integer minY;
    private Integer minZ;
    // 包围盒最大 X/Y/Z。
    private Integer maxX;
    private Integer maxY;
    private Integer maxZ;

    @Override
    public void readInfo(BinaryReader reader) throws IOException {
        minX = reader.readInt();
        minY = reader.readInt();
        minZ = reader.readInt();
        maxX = reader.readInt();
        maxY = reader.readInt();
        maxZ = reader.readInt();
    }

    @Override
    public com.giga.nexas.dto.bsdx.spm.Spm.SPMHitArea transHitbox() {
        int left = Math.min(valueOrZero(minX), valueOrZero(maxX));
        int right = Math.max(valueOrZero(minX), valueOrZero(maxX));
        int top = Math.min(valueOrZero(minY), valueOrZero(maxY));
        int bottom = Math.max(valueOrZero(minY), valueOrZero(maxY));
        int zMin = Math.min(valueOrZero(minZ), valueOrZero(maxZ));
        int zMax = Math.max(valueOrZero(minZ), valueOrZero(maxZ));
        return buildBsdxHitArea(left, top, right, bottom, zMin, zMax, null);
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
