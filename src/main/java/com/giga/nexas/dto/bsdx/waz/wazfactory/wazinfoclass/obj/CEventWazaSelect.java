package com.giga.nexas.dto.bsdx.waz.wazfactory.wazinfoclass.obj;

import com.giga.nexas.io.BinaryReader;
import com.giga.nexas.io.BinaryWriter;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;

/**
 * @Author 这位同学(Karaik)
 * @Date 2025/2/28
 * CEventWazaSelect__Read
 */
@Data
@NoArgsConstructor
public class CEventWazaSelect extends SkillInfoObject {

    // 0是effect.waz，1-5是tama1-5.waz，6是laser.waz,7是bomb.waz
    private Integer wazFileNo;
    // 对应文件的哪一个waz行为单元
    private Integer wazSequenceNo;

    public CEventWazaSelect(Integer typeId) {
        super(typeId);
    }

    public static final String[] CEVENT_WAZA_SELECT_FORMATS = {
            " ： "
    };

    @Override
    public void readInfo(BinaryReader reader) {
        super.readInfo(reader);

        this.wazFileNo = reader.readInt();
        this.wazSequenceNo = reader.readInt();
    }

    @Override
    public void writeInfo(BinaryWriter writer) throws IOException {
        super.writeInfo(writer);
        writer.writeInt(this.wazFileNo);
        writer.writeInt(this.wazSequenceNo);
    }

}

