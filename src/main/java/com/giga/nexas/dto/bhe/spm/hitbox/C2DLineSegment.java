package com.giga.nexas.dto.bhe.spm.hitbox;

import com.giga.nexas.dto.bhe.spm.Spm;
import com.giga.nexas.io.BinaryReader;
import lombok.Data;

import java.io.IOException;

@Data
public class C2DLineSegment extends Spm.SPMHitArea {

    // 线段起点 X。
    private Integer startX;
    // 线段起点 Y。
    private Integer startY;
    // 线段终点 X。
    private Integer endX;
    // 线段终点 Y。
    private Integer endY;
    private byte[] skippedBytes;

    @Override
    public void readInfo(BinaryReader reader) throws IOException {
        startX = reader.readInt();
        startY = reader.readInt();
        endX = reader.readInt();
        endY = reader.readInt();
        skippedBytes = reader.readBytes(8);
    }

    @Override
    public com.giga.nexas.dto.bsdx.spm.Spm.SPMHitArea transHitbox() {
        int sx = valueOrZero(startX);
        int sy = valueOrZero(startY);
        int ex = valueOrZero(endX);
        int ey = valueOrZero(endY);
        int left = Math.min(sx, ex);
        int right = Math.max(sx, ex);
        int top = Math.min(sy, ey);
        int bottom = Math.max(sy, ey);
        return buildBsdxHitArea(left, top, right, bottom, null, null, 3);
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
