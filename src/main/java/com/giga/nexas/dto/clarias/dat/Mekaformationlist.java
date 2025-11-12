package com.giga.nexas.dto.clarias.dat;

import java.util.List;

/**
 * @Author 这位同学(Karaik)
 * @Date 2025/11/12
 * @Description Mekaformationlist
 */
public class Mekaformationlist {
    
    public String fileName = "mekaformationlist";

    /**
     * 文件开头的条目数量（uint32）
     */
    private Integer count;

    /**
     * Formation 条目列表
     */
    private List<Formation> formations;

    /**
     * 单条 Formation 记录
     */
    private static class Formation {

        /**
         * 第一个 1 字节标志（sub_9CD060），是否存在 extra1
         */
        private Integer extra1Flag;

        /**
         * 第一个可选的 FormationExtra 对象
         */
        private FormationExtra extra1;

        /**
         * 第二个 1 字节标志（sub_9CD060），是否存在 extra2
         */
        private Integer extra2Flag;

        /**
         * 第二个可选的 FormationExtra 对象
         */
        private FormationExtra extra2;

        /**
         * 日文名称（Shift-JIS，File::readString(Src)）
         */
        private String nameJp;

        /**
         * 英文 ID（ASCII，File::readString(Src + 6)，例如 Z_CIRCLE、L_FRONT）
         */
        private String code;

        /**
         * 第一个 1 字节 flag（File::readInt(a2, Src + 12, 1)）
         */
        private Integer flag1;

        /**
         * 第二个 1 字节 flag（File::readInt(a2, (char*)Src + 49, 1)）
         */
        private Integer flag2;

        /**
         * 后续 6 个 4 字节整型（File::readInt(a2, Src + 15..20, 4)）
         */
        private Integer int1;

        /**
         * 第二个 4 字节整型
         */
        private Integer int2;

        /**
         * 第三个 4 字节整型
         */
        private Integer int3;

        /**
         * 第四个 4 字节整型
         */
        private Integer int4;

        /**
         * 第五个 4 字节整型
         */
        private Integer int5;

        /**
         * 第六个 4 字节整型
         */
        private Integer int6;
    }

    /**
     * 对应 sub_8B3320 读取的 Extra 结构
     */
    private static class FormationExtra {

        /**
         * 第一个 4 字节整型（File::readInt(this + 4, 4)）
         */
        private Integer int1;

        /**
         * 第一组 int 数组：先读 count，再读 count 个 int32
         */
        private List<Integer> values1;

        /**
         * 第二组 int 数组
         */
        private List<Integer> values2;

        /**
         * 第三组 int 数组
         */
        private List<Integer> values3;

        /**
         * 第四组 int 数组
         */
        private List<Integer> values4;

        /**
         * 第五组为若干 (int,int) 对
         */
        private List<ExtraPair> pairs;

        /**
         * 最后一个 4 字节整型（File::readInt(this + 68, 4)）
         */
        private Integer int2;
    }

    /**
     * FormationExtra 中的 (int,int) 对
     */
    private static class ExtraPair {

        /**
         * 对内第一个整型
         */
        private Integer int1;

        /**
         * 对内第二个整型
         */
        private Integer int2;
    }
}
