package com.giga.nexas.dto.bhe.spm.parser;

import com.giga.nexas.dto.bhe.spm.Spm;
import com.giga.nexas.dto.bhe.BheParser;
import com.giga.nexas.dto.bhe.spm.hitbox.DefaultHitArea;
import com.giga.nexas.dto.bhe.spm.hitbox.HitBoxFactory;
import com.giga.nexas.io.BinaryReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 仅为了阅读和维护方便，整体从bsdx中复制而来，有细微修改
 */
public class SpmParser implements BheParser<Spm> {

//    private static final String SPM_VERSION_202 = "SPM VER-2.02";
//    private static String currentVersion;

    @Override
    public String supportExtension() {
        return "spm";
    }

    @Override
    public Spm parse(byte[] data, String filename, String charset) throws IOException {
        BinaryReader reader = new BinaryReader(data, charset);
        Spm spm = new Spm();

        spm.setSpmVersion(reader.readNullTerminatedString());
//        currentVersion = spm.getSpmVersion();

        spm.setNumPageData(reader.readInt());

        List<Spm.SPMPageData> pageDataList = new ArrayList<>();
        for (int i = 0; i < spm.getNumPageData(); i++) {
            pageDataList.add(parsePageData(reader));
        }
        spm.setPageData(pageDataList);

        spm.setNumImageData(reader.readInt());

        List<Spm.SPMImageData> imageDataList = new ArrayList<>();
        for (int i = 0; i < spm.getNumImageData(); i++) {
            imageDataList.add(parseImageData(reader));
        }
        spm.setImageData(imageDataList);

        spm.setPatPageNum(reader.readInt());
        spm.setNumAnimData(reader.readInt());

        List<Spm.SPMAnimData> animDataList = new ArrayList<>();
        for (int i = 0; i < spm.getNumAnimData(); i++) {
            animDataList.add(parseAnimData(reader, spm.getPatPageNum()));
        }
        spm.setAnimData(animDataList);

        return spm;
    }

    private Spm.SPMPageData parsePageData(BinaryReader reader) throws IOException {
        Spm.SPMPageData page = new Spm.SPMPageData();
        page.setNumChipData(reader.readInt());
        page.setPageWidth(reader.readInt());
        page.setPageHeight(reader.readInt());
        page.setPageRect(parseRect(reader));
        page.setPageOption(reader.readInt() & 0xFFFFFFFFL);
        page.setRotateCenterX(reader.readInt());
        page.setRotateCenterY(reader.readInt());
        page.setHitFlag(reader.readInt() & 0xFFFFFFFFL);

        // SPM2.02
//        if (SPM_VERSION_202.equals(currentVersion)) {
//            page.setUnk3(reader.readByte());
//        }

        List<Spm.SPMHitArea> hitRects = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            if (((1L << (i & 31)) & page.getHitFlag()) != 0L) {
                hitRects.add(parseHitArea(reader));
            }
        }
        page.setHitRects(hitRects);

        List<Spm.SPMChipData> chipData = new ArrayList<>();
        for (int i = 0; i < page.getNumChipData(); i++) {
            chipData.add(parseChipData(reader));
        }
        page.setChipData(chipData);

        return page;
    }

    private Spm.SPMRect parseRect(BinaryReader reader) {
        Spm.SPMRect rect = new Spm.SPMRect();
        rect.setLeft(reader.readInt());
        rect.setTop(reader.readInt());
        rect.setRight(reader.readInt());
        rect.setBottom(reader.readInt());
        return rect;
    }

    private Spm.SPMHitArea parseHitArea(BinaryReader reader) throws IOException {
        short id = reader.readShort();
        short typeId = reader.readShort();

        Spm.SPMHitArea shapeObj = HitBoxFactory.createShapeObj(typeId);
        shapeObj.setId(id);
        shapeObj.setShapeType(typeId);

        if (typeId!=0) {
            shapeObj.readInfo(reader);
        } else {
            shapeObj = new DefaultHitArea();
            shapeObj.readInfo(reader);
        }

        return shapeObj;
    }

    private Spm.SPMChipData parseChipData(BinaryReader reader) {
        Spm.SPMChipData chipData = new Spm.SPMChipData();
        chipData.setImageNo(reader.readInt());
        chipData.setDstRect(parseRect(reader));
        chipData.setChipWidth(reader.readInt());
        chipData.setChipHeight(reader.readInt());
        chipData.setSrcRect(parseRect(reader));
        chipData.setDrawOption(reader.readInt() & 0xFFFFFFFFL);

        // SPM2.02
//        if (SPM_VERSION_202.equals(currentVersion)) {
//            chipData.setUnk5(reader.readByte());
//        }

        chipData.setDrawOptionValue(reader.readInt() & 0xFFFFFFFFL);
        chipData.setOption(reader.readInt());
        return chipData;
    }

    // 解析SPMImageData
    private Spm.SPMImageData parseImageData(BinaryReader reader) {
        Spm.SPMImageData imageData = new Spm.SPMImageData();
        imageData.setImageName(reader.readNullTerminatedString());
        return imageData;
    }

    // 解析SPMAnimData
    private Spm.SPMAnimData parseAnimData(BinaryReader reader, int patPageNum) {
        Spm.SPMAnimData animData = new Spm.SPMAnimData();
        animData.setAnimName(reader.readNullTerminatedString());
        animData.setNumPat(reader.readInt());
        animData.setAnimRotateDirection(reader.readInt());
        animData.setAnimReverseDirection(reader.readInt());

        int numPat = animData.getNumPat();  // diff
        List<Spm.SPMPatData> patDataList = new ArrayList<>();
        for (int i = 0; i < numPat; i++) {
            patDataList.add(parsePatData(reader, patPageNum));
        }
        animData.setPatData(patDataList);

        return animData;
    }

    private Spm.SPMPatData parsePatData(BinaryReader reader, int patPageNum) {
        Spm.SPMPatData patData = new Spm.SPMPatData();
        patData.setWaitFrame(reader.readInt());
        List<Integer> pageNos = new ArrayList<>();
        for (int i = 0; i < patPageNum; i++) {
            pageNos.add(reader.readInt());
        }
        patData.setPageNo(pageNos);
        return patData;
    }
}
