package com.giga.nexas.dto.bhe.waz.wazfactory.wazinfoclass.obj;

import com.giga.nexas.dto.bhe.BheInfoCollection;
import com.giga.nexas.io.BinaryReader;
import com.giga.nexas.io.BinaryWriter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class CEventSpriteYure extends SkillInfoObject {

    @Data
    @AllArgsConstructor
    public static class CEventSpriteYureType {
        private Integer type;
        private String description;
    }

    public static final CEventSpriteYureType[] CEVENT_SPRITE_YURE_TYPES = {
            new CEventSpriteYureType(0xFFFFFFFF, "角度指定"),
            new CEventSpriteYureType(0xFFFFFFFF, "左回転"),
            new CEventSpriteYureType(0xFFFFFFFF, "右回転"),
            new CEventSpriteYureType(0xFFFFFFFF, "下"),
            new CEventSpriteYureType(0xFFFFFFFF, "上"),
            new CEventSpriteYureType(0xFFFFFFFF, "左"),
            new CEventSpriteYureType(0xFFFFFFFF, "右"),
            new CEventSpriteYureType(0xFFFFFFFF, "ランダム")
    };

    public static final String[] CEVENT_SPRITE_YURE_FORMATS = {
            "%s : %4d (周期 : %4d)",
            "%s : %4d ",
            "⇒ %4d (周期 : %4d)"
    };


    private List<BheInfoCollection> bheInfoCollectionList = new ArrayList<>();

    private Integer vibrationAreaType;
    private Integer vibrationAmplitudeOutToIn;
    private Integer vibrationAmplitudeInToOut;
    private Integer vibrationDampingFactor;

    public CEventSpriteYure(Integer typeId) {
        super(typeId);
    }

    @Override
    public void readInfo(BinaryReader reader) {
        super.readInfo(reader);

        this.bheInfoCollectionList.clear();
        BheInfoCollection bheInfoCollection = new BheInfoCollection();
        bheInfoCollection.readCollection(reader);
        this.bheInfoCollectionList.add(bheInfoCollection);

        this.vibrationAreaType = reader.readInt();
        this.vibrationAmplitudeOutToIn = reader.readInt();
        this.vibrationAmplitudeInToOut = reader.readInt();
        this.vibrationDampingFactor = reader.readInt();
    }

    @Override
    public void writeInfo(BinaryWriter writer) throws IOException {
        super.writeInfo(writer);
        for (BheInfoCollection collection : bheInfoCollectionList) {
            collection.writeCollection(writer);
        }
        writer.writeInt(this.vibrationAreaType);
        writer.writeInt(this.vibrationAmplitudeOutToIn);
        writer.writeInt(this.vibrationAmplitudeInToOut);
        writer.writeInt(this.vibrationDampingFactor);
    }

}
