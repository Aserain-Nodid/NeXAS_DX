package com.giga.nexas.dto.clarias;

import java.io.IOException;

public interface ClariasGenerator<T extends Clarias> {
    String supportExtension();
    void generate(String path, T t, String charset) throws IOException;
}
