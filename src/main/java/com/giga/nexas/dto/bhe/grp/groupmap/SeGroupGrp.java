package com.giga.nexas.dto.bhe.grp.groupmap;

import com.giga.nexas.dto.bhe.grp.Grp;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SeGroupGrp extends Grp {

    private List<SeGroupGroup> seList = new ArrayList<>();

    @Data
    public static class SeGroupGroup {
        public Integer existFlag; // 仅记录用
        private String seType;
        private String seTypeCodeName;
        private List<SeGroupItem> seItems = new ArrayList<>();
    }

    @Data
    public static class SeGroupItem {
        public Integer existFlag; // 仅记录用
        private String seItemName;
        private String seItemCodeName;
        private String seFileName;
    }
}
