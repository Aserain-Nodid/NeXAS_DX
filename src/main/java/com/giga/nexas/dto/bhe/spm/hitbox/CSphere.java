package com.giga.nexas.dto.bhe.spm.hitbox;

import com.giga.nexas.dto.bhe.spm.Spm;
import com.giga.nexas.io.BinaryReader;
import lombok.Data;

import java.io.IOException;

@Data
public class CSphere extends Spm.SPMHitArea {

    // 球心坐标。
    private Integer centerX;
    private Integer centerY;
    private Integer centerZ;
    // 半径。
    private Integer radius;

    @Override
    public void readInfo(BinaryReader reader) throws IOException {
        centerX = reader.readInt();
        centerY = reader.readInt();
        centerZ = reader.readInt();
        radius = reader.readInt();
    }

    @Override
    public com.giga.nexas.dto.bsdx.spm.Spm.SPMHitArea transHitbox() {
        int r = valueOrZero(radius);
        int cx = valueOrZero(centerX);
        int cy = valueOrZero(centerY);
        int cz = valueOrZero(centerZ);
        int left = cx - r;
        int right = cx + r;
        int top = cy - r;
        int bottom = cy + r;
        int zMin = cz - r;
        int zMax = cz + r;
        return buildBsdxHitArea(left, top, right, bottom, zMin, zMax, null);
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
