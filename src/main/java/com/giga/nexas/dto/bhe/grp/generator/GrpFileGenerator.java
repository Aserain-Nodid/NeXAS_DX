package com.giga.nexas.dto.bhe.grp.generator;

import com.giga.nexas.dto.bhe.grp.Grp;
import com.giga.nexas.io.BinaryWriter;

import java.io.IOException;

public interface GrpFileGenerator<T extends Grp> {
    String getGeneratorKey();
    void generate(BinaryWriter writer, Grp grp) throws IOException;
}
