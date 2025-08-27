package com.giga.nexas.dto.bhe.grp.parser.impl;

import com.giga.nexas.dto.bhe.grp.Grp;
import com.giga.nexas.dto.bhe.grp.groupmap.WazaGroupGrp;
import com.giga.nexas.dto.bhe.grp.parser.GrpFileParser;
import com.giga.nexas.io.BinaryReader;

public class WazaGroupGrpParser implements GrpFileParser<Grp> {

    @Override
    public String getParserKey() {
        return "wazagroup";
    }

    @Override
    public Grp parse(BinaryReader reader) {
        WazaGroupGrp wazaGroupGrp = new WazaGroupGrp();

        int groupCount = reader.readInt();
        for (int i = 0; i < groupCount; i++) {
            WazaGroupGrp.WazaGroupEntry entry = new WazaGroupGrp.WazaGroupEntry();
            int flag = reader.readInt();
            entry.setExistFlag(flag);
            if (flag != 0) {
                entry.setWazaName(reader.readNullTerminatedString());
                entry.setWazaCodeName(reader.readNullTerminatedString());
                entry.setWazaDisplayName(reader.readNullTerminatedString());
                entry.setParam(reader.readInt());
            }
            wazaGroupGrp.getWazaList().add(entry);
        }

        return wazaGroupGrp;
    }
}
