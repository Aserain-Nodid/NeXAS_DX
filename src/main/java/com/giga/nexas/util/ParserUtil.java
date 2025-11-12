package com.giga.nexas.util;


/**
 * @Author 这位同学(Karaik)
 */
public class ParserUtil {

    // .dat
    // 数值
    public static final int DAT_COLUMN_TYPE_DATA = 0;
    // 字符串
    public static final int DAT_COLUMN_TYPE_STRING = 1;
    // 也是数值，疑似代表新引擎的int
    public static final int DAT_COLUMN_TYPE_INT_NEW = 2;
    // 闪钢中存在
    public static final int DAT_COLUMN_TYPE_BYTE = 3;
    // 字符串
    public static final String TYPE_STRING  = "String";
    public static final String TYPE_INT     = "Integer";
    public static final String TYPE_INT_NEW = "IntegerNew";
    public static final String TYPE_BYTE = "Byte";

    /**
     * 判断句子是否大致上为日语
     */
    public static boolean isLikelyJapanese(String text) {
        int japaneseCharCount = 0;
        for (char c : text.toCharArray()) {
            if ((c >= '\u3040' && c <= '\u309F') || // 平假名
                    (c >= '\u30A0' && c <= '\u30FF') || // 片假名
                    (c >= '\u4E00' && c <= '\u9FFF') || // CJK 统一汉字（包括日语汉字）
                    (c >= '\uFF66' && c <= '\uFF9F')) { // 半角片假名
                japaneseCharCount++;
            }
        }
        // 阈值
        return (japaneseCharCount > text.length() * 0.5);
    }

}
