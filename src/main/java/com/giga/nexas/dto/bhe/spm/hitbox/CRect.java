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
        // read 0x10u
        rect = new Spm.SPMRect();
        rect.setLeft(reader.readInt());
        rect.setTop(reader.readInt());
        rect.setRight(reader.readInt());
        rect.setBottom(reader.readInt());

        // 8 bytes reserved
        skipped = reader.readLong();
    }

}
