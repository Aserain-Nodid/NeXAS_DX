package com.giga.nexas.dto.bhe.waz.wazfactory.wazinfoclass.obj;

import com.giga.nexas.dto.bhe.BheInfoCollection;
import com.giga.nexas.io.BinaryReader;
import com.giga.nexas.io.BinaryWriter;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class CEventTerm extends SkillInfoObject {

    private List<BheInfoCollection> bheInfoCollectionList = new ArrayList<>();

    public CEventTerm(Integer typeId) {
        super(typeId);
    }

    @Override
    public void readInfo(BinaryReader reader) {
        super.readInfo(reader);

        this.bheInfoCollectionList.clear();
        BheInfoCollection bheInfoCollection = new BheInfoCollection();
        bheInfoCollection.readCollection(reader);
        this.bheInfoCollectionList.add(bheInfoCollection);
    }

    @Override
    public void writeInfo(BinaryWriter writer) throws IOException {
        super.writeInfo(writer);
        for (BheInfoCollection collection : bheInfoCollectionList) {
            collection.writeCollection(writer);
        }
    }

}
