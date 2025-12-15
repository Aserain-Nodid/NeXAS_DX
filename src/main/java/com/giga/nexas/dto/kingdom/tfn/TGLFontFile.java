package com.giga.nexas.dto.kingdom.tfn;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * グラフィックフォント
 * Version 1: TGLFontFile
 * ASCII/Kana: 1bpp but uses half-width stride
 * Kanji: 1bpp uses full-width stride with (width & 0xF) alignment rule
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TGLFontFile extends Tfn {

    @Override
    public int getFixedBlockSize() {
        // v6 = ceil((width / 2) / 8)
        int width = getWidth();
        int height = getHeight();

        int halfWidth = width >> 1;

        int rowStride = (halfWidth >> 3);
        // if ((halfWidth & 7) != 0) rowStride++
        if ( (halfWidth & 7) != 0 ) {
            rowStride += 1;
        }

        return rowStride * height;
    }

    @Override
    public int getVariableBlockSize() {
        // v7 = width / 8 + ((width & 0xF) != 0)
        int width = getWidth();
        int height = getHeight();

        int rowStride = (width >> 3);
        // if ((width & 0xF) != 0) rowStride++
        if ( (width & 0xF) != 0 ) {
            rowStride += 1;
        }

        return rowStride * height;
    }
}