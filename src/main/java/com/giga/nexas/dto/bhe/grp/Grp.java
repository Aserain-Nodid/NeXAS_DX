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
        @JsonSubTypes.Type(value = BatVoiceGrp.class, name = "batvoice"),
        @JsonSubTypes.Type(value = MapGroupGrp.class, name = "mapgroup"),
        @JsonSubTypes.Type(value = MekaGroupGrp.class, name = "mekagroup"),
        @JsonSubTypes.Type(value = ProgramMaterialGrp.class, name = "programmaterial"),
        @JsonSubTypes.Type(value = SeGroupGrp.class, name = "segroup"),
        @JsonSubTypes.Type(value = SpriteGroupGrp.class, name = "spritegroup"),
        @JsonSubTypes.Type(value = TermGrp.class, name = "term"),
        @JsonSubTypes.Type(value = WazaGroupGrp.class, name = "wazagroup")
})
@Data
public class Grp extends Bhe {

    // 文件名
    private String fileName;

}
