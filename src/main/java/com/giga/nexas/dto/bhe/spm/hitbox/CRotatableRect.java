package com.giga.nexas.dto.bhe.spm.hitbox;

import com.giga.nexas.dto.bhe.spm.Spm;
import com.giga.nexas.io.BinaryReader;
import lombok.Data;

import java.io.IOException;

@Data
public class CRotatableRect extends Spm.SPMHitArea {

    // 矩形中心点的 X 坐标。
    private Integer centerX;
    // 矩形中心点的 Y 坐标。
    private Integer centerY;
    private byte[] skippedBytes1;
    // 矩形宽度（像素）。
    private Integer width;
    // 矩形高度（像素）。
    private Integer height;
    private byte[] skippedBytes2;
    // 低 16 位属性标志，旋转信息可能保存在此处。
    private Integer propertyFlags;

    @Override
    public void readInfo(BinaryReader reader) throws IOException {
        centerX = reader.readInt();
        centerY = reader.readInt();
        skippedBytes1 = reader.readBytes(4);
        width = reader.readInt();
        height = reader.readInt();
        skippedBytes2 = reader.readBytes(4);
        int raw = reader.readInt();
        propertyFlags = raw & 0xFFFF;
    }

    @Override
    public com.giga.nexas.dto.bsdx.spm.Spm.SPMHitArea transHitbox() {
        int w = valueOrZero(width);
        int h = valueOrZero(height);
        int cx = valueOrZero(centerX);
        int cy = valueOrZero(centerY);
        int left = cx - w / 2;
        int top = cy - h / 2;
        int right = left + w;
        int bottom = top + h;
        return buildBsdxHitArea(left, top, right, bottom, null, null, null);
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
