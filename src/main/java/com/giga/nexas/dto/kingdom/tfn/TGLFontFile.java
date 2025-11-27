package com.giga.nexas.dto.kingdom.tfn;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Version 1: TGLFontFile
 * All glyphs are 1bpp (1 bit per pixel)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TGLFontFile extends Tfn {

    @Override
    public int getFixedBlockSize() {
        return calculate1bppSize();
    }

    @Override
    public int getVariableBlockSize() {
        return calculate1bppSize();
    }

    private int calculate1bppSize() {
        // ASM Logic: v7 = v5 / 8 + ((v5 & 0xF) != 0);
        // TGL 1bpp specific alignment
        int w = getWidth();
        int rowStride = (w / 8) + ((w & 0xF) != 0 ? 1 : 0);
        return rowStride * getHeight();
    }
}