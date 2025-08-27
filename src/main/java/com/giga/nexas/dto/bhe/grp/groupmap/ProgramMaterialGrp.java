package com.giga.nexas.dto.bhe.grp.groupmap;

import com.giga.nexas.dto.bhe.grp.Grp;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProgramMaterialGrp extends Grp {

    private List<IntArray> array1 = new ArrayList<>();
    private List<IntArray> array2 = new ArrayList<>();
    private List<IntArray> array3 = new ArrayList<>();

    @Data
    public static class IntArray {
        private List<Integer> values = new ArrayList<>();
    }

}
