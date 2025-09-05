package com.giga.nexas.dto.bhe.grp.groupmap;

import com.giga.nexas.dto.bhe.grp.Grp;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class BatVoiceGrp extends Grp {

    private List<BatVoiceTypeGroup> voiceTypeList = new ArrayList<>();
    private List<BatVoiceGroup> voiceList = new ArrayList<>();

    @Data
    public static class BatVoiceTypeGroup {
        private String voiceType;
        private String voiceTypeCodeName;
    }

    @Data
    public static class BatVoiceGroup {
        public Integer existFlag; // 仅记录用
        private String characterName;
        private Integer unk0; // diff
        private String characterCodeName;
        private List<BatVoice> voices = new ArrayList<>();
    }

    @Data
    public static class BatVoice {
        public Integer existFlag; // 仅记录用
        private String voice;
        private String voiceCodeName;
        private String voiceFileName;
    }

}
