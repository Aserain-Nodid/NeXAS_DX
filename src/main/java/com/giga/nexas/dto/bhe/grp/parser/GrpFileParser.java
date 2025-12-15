package com.giga.nexas.dto.bhe.grp.parser;

import com.giga.nexas.dto.bhe.grp.Grp;
import com.giga.nexas.io.BinaryReader;

public interface GrpFileParser<T extends Grp> {
    String getParserKey();
    T parse(BinaryReader reader);
}
