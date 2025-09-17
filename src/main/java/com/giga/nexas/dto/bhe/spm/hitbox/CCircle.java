package com.giga.nexas.dto.bhe.spm.hitbox;

import com.giga.nexas.dto.bhe.spm.Spm;
import com.giga.nexas.io.BinaryReader;
import lombok.Data;

import java.io.IOException;

@Data
public class CCircle extends Spm.SPMHitArea {

    // 圆心 X 坐标。
    private Integer centerX;
    // 圆心 Y 坐标。
    private Integer centerY;
    private byte[] skippedBytes;
    // 半径。
    private Integer radius;

    @Override
    public void readInfo(BinaryReader reader) throws IOException {
        centerX = reader.readInt();
        centerY = reader.readInt();
        skippedBytes = reader.readBytes(8);
        radius = reader.readInt();
    }

    @Override
    public com.giga.nexas.dto.bsdx.spm.Spm.SPMHitArea transHitbox() {
        int r = valueOrZero(radius);
        int cx = valueOrZero(centerX);
        int cy = valueOrZero(centerY);
        int left = cx - r;
        int right = cx + r;
        int top = cy - r;
        int bottom = cy + r;
        return buildBsdxHitArea(left, top, right, bottom, null, null, null);
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
