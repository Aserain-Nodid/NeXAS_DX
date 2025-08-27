package com.giga.nexas.dto.bhe.grp;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.giga.nexas.dto.bhe.grp.groupmap.*;
import com.giga.nexas.dto.bhe.Bhe;
import lombok.Data;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "fileName",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BatVoiceGrp.class, name = "BatVoice"),
        @JsonSubTypes.Type(value = MapGroupGrp.class, name = "MapGroup"),
        @JsonSubTypes.Type(value = MekaGroupGrp.class, name = "MekaGroup"),
        @JsonSubTypes.Type(value = ProgramMaterialGrp.class, name = "ProgramMaterial"),
        @JsonSubTypes.Type(value = SeGroupGrp.class, name = "SeGroup"),
        @JsonSubTypes.Type(value = SpriteGroupGrp.class, name = "SpriteGroup"),
        @JsonSubTypes.Type(value = TermGrp.class, name = "Term"),
        @JsonSubTypes.Type(value = WazaGroupGrp.class, name = "WazaGroup")
})
@Data
public class Grp extends Bhe {

    // 文件名
    private String fileName;

}
