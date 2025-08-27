package com.giga.nexas.dto.bhe.grp.groupmap;

import com.giga.nexas.dto.bhe.grp.Grp;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SpriteGroupGrp extends Grp {

    private List<SpriteGroupEntry> spriteList = new ArrayList<>();

    @Data
    public static class SpriteGroupEntry {
        public Integer existFlag; // 仅记录用
        private String spriteFileName;
        private String spriteCodeName;
        private Integer param;
    }

}
