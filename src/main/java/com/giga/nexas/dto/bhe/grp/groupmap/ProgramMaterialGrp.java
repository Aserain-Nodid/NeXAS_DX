package com.giga.nexas.dto.bhe.grp.groupmap;

import com.giga.nexas.dto.bhe.grp.Grp;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProgramMaterialGrp extends Grp {

    private List<HeaderItem> header = new ArrayList<>(); // diff

    private List<PairArray> pairArrary1 = new ArrayList<>();// diff
    private List<IntArray> array2 = new ArrayList<>();
    private List<IntArray> array3 = new ArrayList<>();

    @Data
    public static class HeaderItem {
        private int int1;
        private int int2;
        private int int3;
        private int int4;
    }

    @Data
    public static class IntArray {
        private List<Integer> values = new ArrayList<>();
    }

    @Data
    public static class Pair {
        private int first;
        private int second;
    }

    @Data
    public static class PairArray {
        private List<Pair> values = new ArrayList<>();
    }

}
