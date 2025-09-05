package com.giga.nexas.dto.bhe.grp.groupmap;

import com.giga.nexas.dto.bhe.grp.Grp;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MapGroupGrp extends Grp {

    private List<MapGroup> groupList = new ArrayList<>();

    /**
     * existFlag, 3个字符串, 1个未知int(int1), N个Item(每项4个int), 三段 数组的数组
     */
    @Data
    public static class MapGroup {

        private int existFlag;
        private String groupName;
        private String groupCodeName;
        private String groupResourceName;
        private int int1;

        // 每项5个int
        private List<Item> items = new ArrayList<>();

        private List<PairArray> array1 = new ArrayList<>(); // diff
        private List<IntArray> array2 = new ArrayList<>();
        private List<IntArray> array3 = new ArrayList<>();
    }

    @Data
    public static class Item {
        private int int1;
        private int int2;
        private int int3;
        private int int4;
        private int int5; // diff
    }

    @Data
    public static class IntArray {
        private List<Integer> values = new ArrayList<>();
    }

    @Data
    public static class Pair {
        private int int1;
        private int int2;
    }

    @Data
    public static class PairArray {
        private List<Pair> values = new ArrayList<>();
    }

}
