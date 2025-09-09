package com.giga.nexas.dto.bhe.spm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.giga.nexas.dto.bhe.Bhe;
import com.giga.nexas.dto.bhe.spm.hitbox.*;
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "shapeType", visible = true)
    @JsonSubTypes(value = {
            @JsonSubTypes.Type(value = DefaultHitArea.class, name = "0"),
            @JsonSubTypes.Type(value = CRotatableRect.class, name = "1"),
            @JsonSubTypes.Type(value = CCircle.class, name = "2"),
            @JsonSubTypes.Type(value = C2DLineSegment.class, name = "7"),
            @JsonSubTypes.Type(value = C2DDot.class, name = "8"),
            @JsonSubTypes.Type(value = CBox.class, name = "9"),
            @JsonSubTypes.Type(value = CRotatableBox.class, name = "10"),
            @JsonSubTypes.Type(value = CSphere.class, name = "11")
    })
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
