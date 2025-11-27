package com.giga.nexas.dto.kingdom.tfn;

import com.giga.nexas.dto.kingdom.Kingdom;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * TGL Font File Base Model
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class Tfn extends Kingdom {

    public static final String TFN_TGLFONTFILE = "TGLFontFile";
    public static final String TFN_TGLFONTFILE2 = "TGLFontFile2";

    // 仅记录用
    public String fileName;

    // Header
    private String magic;
    private int width;
    private int height;

    // Maps
    private byte[] mapA; // 0x2000 bytes
    private byte[] mapB; // 0x2000 bytes

    // Data
    // Fixed Block: ASCII (128 chars) & Kana (64 chars)
    private List<byte[]> asciiGlyphs = new ArrayList<>(128);
    private List<byte[]> kanaGlyphs = new ArrayList<>(64);

    // Variable Block: Kanji (Sparse Map)
    // Key: Map Index (Shift-JIS Linear Offset), Value: Raw Bitmap Data
    private Map<Integer, byte[]> kanjiGlyphsA = new LinkedHashMap<>();
    private Map<Integer, byte[]> kanjiGlyphsB = new LinkedHashMap<>();

    /**
     * 计算固定块(ASCII/Kana)单字所需字节数
     */
    public abstract int getFixedBlockSize();

    /**
     * 计算可变块(Kanji)单字所需字节数
     */
    public abstract int getVariableBlockSize();
}