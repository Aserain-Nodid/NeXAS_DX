package com.giga.nexas.dto.bhe.spm;

import com.giga.nexas.dto.bhe.Bhe;
import com.giga.nexas.io.BinaryReader;
import lombok.Data;

import java.io.IOException;
import java.util.List;

@Data
public class Spm extends Bhe {

    private String spmVersion;
    private Integer numPageData;
    private List<SPMPageData> pageData;
    private Integer numImageData;
    private List<SPMImageData> imageData;
    private Integer patPageNum;
    private Integer numAnimData;
    private List<SPMAnimData> animData;

    @Data
    public static class SPMRect {
        private Integer left;
        private Integer top;
        private Integer right;
        private Integer bottom;
    }

    @Data
    public static class SPMChipData {
        private Integer imageNo;
        private SPMRect dstRect;
        private Integer chipWidth;
        private Integer chipHeight;
        private SPMRect srcRect;
        private Long drawOption;
//        private byte unk5; // SPM2.02
        private Long drawOptionValue;
        private Integer option;
    }

    @Data
    public static class SPMPageData {
        private Integer numChipData;
        private Integer pageWidth;
        private Integer pageHeight;
        private SPMRect pageRect;
        private Long pageOption;
        private Integer rotateCenterX;
        private Integer rotateCenterY;
        private Long hitFlag;
//        private byte unk3; // SPM2.02
        private List<SPMHitArea> hitRects;
        private List<SPMChipData> chipData;
    }

    @Data
    public static class SPMHitArea {
        private Short id;         // u16
        private Short shapeType;  // u16

        public void readInfo(BinaryReader reader) throws IOException {
            // 空实现用于继承
        }
    }

    @Data
    public static class SPMImageData {
        private String imageName;
    }

    @Data
    public static class SPMPatData {
        private Integer waitFrame;
        private List<Integer> pageNo; // size = patPageNum
    }

    @Data
    public static class SPMAnimData {
        private String animName;
        private Integer numPat;
        private Integer animRotateDirection;
        private Integer animReverseDirection;
        private List<SPMPatData> patData;
    }

    @Data
    public static class SPMTailData {

    }

}
