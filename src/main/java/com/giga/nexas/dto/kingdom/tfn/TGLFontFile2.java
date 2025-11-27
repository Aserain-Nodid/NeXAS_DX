package com.giga.nexas.dto.kingdom.tfn;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * アンチエイリアスフォント
 * Version 2: TGLFontFile2 (Anti-aliased)
 * ASCII/Kana: 2bpp, but stride is computed via v6 = width/2, then ceil(v6/2)
 * Kanji: 4bpp, stride is ceil(width/2)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TGLFontFile2 extends Tfn {

    @Override
    public int getFixedBlockSize() {
        // v6 = width / 2
        // v7 = v6 / 2 + (v6 & 1)
        int width = getWidth();
        int height = getHeight();

        int halfWidth = width >> 1;

        int rowStride = (halfWidth >> 1) + (halfWidth & 1);

        return rowStride * height;
    }

    @Override
    public int getVariableBlockSize() {
        // v5 = width / 2 + (width & 1)
        int width = getWidth();
        int height = getHeight();

        int rowStride = (width >> 1) + (width & 1);

        return rowStride * height;
    }
}
