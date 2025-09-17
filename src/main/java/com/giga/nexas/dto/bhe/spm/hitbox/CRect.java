package com.giga.nexas.dto.bhe.spm.hitbox;

import com.giga.nexas.dto.bhe.spm.Spm;
import com.giga.nexas.io.BinaryReader;
import lombok.Data;

import java.io.IOException;

/**
 * 逻辑上走不通，置空
 */
@Data
public class CRect extends Spm.SPMHitArea {

    private Spm.SPMRect rect;
    private Long skipped;

    @Override
    public void readInfo(BinaryReader reader) throws IOException {
        rect = new Spm.SPMRect();
        rect.setLeft(reader.readInt());
        rect.setTop(reader.readInt());
        rect.setRight(reader.readInt());
        rect.setBottom(reader.readInt());
        skipped = reader.readLong();
    }

    @Override
    public com.giga.nexas.dto.bsdx.spm.Spm.SPMHitArea transHitbox() {
        int left = rect != null && rect.getLeft() != null ? rect.getLeft() : 0;
        int top = rect != null && rect.getTop() != null ? rect.getTop() : 0;
        int right = rect != null && rect.getRight() != null ? rect.getRight() : left;
        int bottom = rect != null && rect.getBottom() != null ? rect.getBottom() : top;
        return buildBsdxHitArea(left, top, right, bottom, null, null, null);
    }
}
