package com.giga.nexas.dto.bhe.spm.hitbox;

import com.giga.nexas.dto.bhe.spm.Spm;
import com.giga.nexas.io.BinaryReader;
import lombok.Data;

import java.io.IOException;

@Data
public class DefaultHitArea extends Spm.SPMHitArea {

    // 区域在页面坐标系中的最小 X。
    private Integer minX;
    // 区域在页面坐标系中的最大 X。
    private Integer maxX;
    // 区域在页面坐标系中的最小 Y。
    private Integer minY;
    // 区域在页面坐标系中的最大 Y。
    private Integer maxY;
    // 区域在页面坐标系中的最小 Z。
    private Integer minZ;
    // 区域在页面坐标系中的最大 Z。
    private Integer maxZ;
    @Override
    public void readInfo(BinaryReader reader) throws IOException {
        minX = reader.readInt();
        maxX = reader.readInt();
        minY = reader.readInt();
        maxY = reader.readInt();
        minZ = reader.readInt();
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
        return buildBsdxHitArea(left, top, right, bottom, zMin, zMax, 1);
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
