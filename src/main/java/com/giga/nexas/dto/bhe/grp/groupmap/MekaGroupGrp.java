package com.giga.nexas.dto.bhe.grp.groupmap;

import com.giga.nexas.dto.bhe.grp.Grp;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MekaGroupGrp extends Grp {

    private List<MekaGroup> mekaList = new ArrayList<>();

    @Data
    public static class MekaGroup {
        public Integer existFlag; // 仅记录用
        private String mekaName;
        private String mekaCodeName;
    }
}
