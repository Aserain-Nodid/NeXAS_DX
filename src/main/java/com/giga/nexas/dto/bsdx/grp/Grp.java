package com.giga.nexas.dto.bsdx.grp;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.giga.nexas.dto.bsdx.Bsdx;
import lombok.Data;

/**
 * @Author 这位同学(Karaik)
 * @Date 2025/2/2
 * @Description GroupMap
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "fileName",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = com.giga.nexas.dto.bsdx.grp.groupmap.BatVoiceGrp.class, name = "batvoice"),
        @JsonSubTypes.Type(value = com.giga.nexas.dto.bsdx.grp.groupmap.MapGroupGrp.class, name = "mapgroup"),
        @JsonSubTypes.Type(value = com.giga.nexas.dto.bsdx.grp.groupmap.MekaGroupGrp.class, name = "mekagroup"),
        @JsonSubTypes.Type(value = com.giga.nexas.dto.bsdx.grp.groupmap.ProgramMaterialGrp.class, name = "programmaterial"),
        @JsonSubTypes.Type(value = com.giga.nexas.dto.bsdx.grp.groupmap.SeGroupGrp.class, name = "segroup"),
        @JsonSubTypes.Type(value = com.giga.nexas.dto.bsdx.grp.groupmap.SpriteGroupGrp.class, name = "spritegroup"),
        @JsonSubTypes.Type(value = com.giga.nexas.dto.bsdx.grp.groupmap.TermGrp.class, name = "term"),
        @JsonSubTypes.Type(value = com.giga.nexas.dto.bsdx.grp.groupmap.WazaGroupGrp.class, name = "wazagroup")
})
@Data
public class Grp extends Bsdx {

    // 文件名
    private String fileName;

}
