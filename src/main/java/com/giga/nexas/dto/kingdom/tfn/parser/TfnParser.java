package com.giga.nexas.dto.kingdom.tfn.parser;

import com.giga.nexas.dto.kingdom.KingdomParser;
import com.giga.nexas.dto.kingdom.tfn.TGLFontFile;
import com.giga.nexas.dto.kingdom.tfn.TGLFontFile2;
import com.giga.nexas.dto.kingdom.tfn.Tfn;
import com.giga.nexas.exception.OperationException;
import com.giga.nexas.io.BinaryReader;

import java.util.Map;

public class TfnParser implements KingdomParser<Tfn> {

    @Override
    public String supportExtension() {
        return "tfn";
    }

    @Override
    public Tfn parse(byte[] data, String filename, String charset) {
        BinaryReader reader = new BinaryReader(data);

        // 1. Magic Check & Factory
        String magicHeader = new String(reader.readBytes(12));

        Tfn tfn;

        if (Tfn.TFN_TGLFONTFILE2.equals(magicHeader)) {
            // V2: "TGLFontFile2" (12 bytes)
            tfn = new TGLFontFile2();
            tfn.setMagic(Tfn.TFN_TGLFONTFILE2);
        } else if (magicHeader.startsWith(Tfn.TFN_TGLFONTFILE)) {
            // V1: "TGLFontFile" (11 bytes)
            tfn = new TGLFontFile();
            tfn.setMagic(Tfn.TFN_TGLFONTFILE);
            reader.seek(11);
        } else {
            throw new OperationException(500, "Unsupported Tfn magic : " + magicHeader);
        }

        // Public field access
        tfn.fileName = filename;

        // 2. Read Header
        tfn.setWidth(reader.readInt());
        tfn.setHeight(reader.readInt());

        // 3. Read Maps
        tfn.setMapA(checkMap(reader.readBytes(0x2000)));
        tfn.setMapB(checkMap(reader.readBytes(0x2000)));


        // 4. Read Fixed Data (ASCII & Kana)
        int fixedSize = tfn.getFixedBlockSize();

        for (int i = 0; i < 128; i++) {
            tfn.getAsciiGlyphs().add(reader.readBytes(fixedSize));
        }

        for (int i = 0; i < 64; i++) {
            tfn.getKanaGlyphs().add(reader.readBytes(fixedSize));
        }

        // 5. Read Variable Data (Kanji)
        int variableSize = tfn.getVariableBlockSize();

        readVariableData(reader, tfn.getMapA(), tfn.getKanjiGlyphsA(), variableSize);
        readVariableData(reader, tfn.getMapB(), tfn.getKanjiGlyphsB(), variableSize);

//        if (reader.remaining() != 0) {
//            throw new OperationException(500, "Tfn read error remaining bytes: " + reader.remaining());
//        }

        return tfn;
    }

    private void readVariableData(BinaryReader reader, byte[] map, Map<Integer, byte[]> targetStorage, int blockSize) {
        for (int i = 0; i < map.length; i++) {
            // Check existence in sparse map
            if (map[i] == 1) {
                byte[] glyphData = reader.readBytes(blockSize);
                targetStorage.put(i, glyphData);
            }
        }
    }

    private byte[] checkMap(byte[] map) {
        for (int i = 0; i < map.length; i++) {
            if ( map[i] != 0x0 && map[i] != 0x1 ) {
                throw new OperationException(500, "Tfn read error map: " + map[i]);
            }
        }
        return map;
    }

}