package com.giga.nexas.dto.bhe.grp.groupmap;

import com.giga.nexas.dto.bhe.grp.Grp;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WazaGroupGrp extends Grp {

    private List<WazaGroupEntry> wazaList = new ArrayList<>();

    @Data
    public static class WazaGroupEntry {
        public Integer existFlag; // 仅记录用
        private String wazaName;
        private String wazaCodeName;
        private String wazaDisplayName;
        private Integer param;
    }
}
