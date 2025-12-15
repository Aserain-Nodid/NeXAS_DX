package com.giga.nexas.dto.bhe.spm.hitbox;

import com.giga.nexas.dto.bhe.spm.Spm;
import com.giga.nexas.io.BinaryReader;
import lombok.Data;

import java.io.IOException;

@Data
public class C2DDot extends Spm.SPMHitArea {

    // 点的 X 坐标。
    private Integer pointX;
    // 点的 Y 坐标。
    private Integer pointY;
    private byte[] skippedBytes;

    @Override
    public void readInfo(BinaryReader reader) throws IOException {
        pointX = reader.readInt();
        pointY = reader.readInt();
        skippedBytes = reader.readBytes(16);
    }

    @Override
    public com.giga.nexas.dto.bsdx.spm.Spm.SPMHitArea transHitbox() {
        int x = valueOrZero(pointX);
        int y = valueOrZero(pointY);
        return buildBsdxHitArea(x, y, x, y, null, null, 0);
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
