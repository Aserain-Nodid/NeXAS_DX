package com.giga.nexas.dto.kingdom.tfn;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Version 2: TGLFontFile2 (Anti-aliased)
 * ASCII/Kana: 2bpp
 * Kanji: 4bpp
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TGLFontFile2 extends Tfn {

    @Override
    public int getFixedBlockSize() {
        // 2bpp: 2 bits per pixel -> 4 pixels per byte
        // ASM Logic equivalent to ceil(width / 4)
        int rowStride = (getWidth() + 3) / 4;
        return rowStride * getHeight();
    }

    @Override
    public int getVariableBlockSize() {
        // 4bpp: 4 bits per pixel -> 2 pixels per byte
        // ASM Logic equivalent to ceil(width / 2)
        int rowStride = (getWidth() + 1) / 2;
        return rowStride * getHeight();
    }
}