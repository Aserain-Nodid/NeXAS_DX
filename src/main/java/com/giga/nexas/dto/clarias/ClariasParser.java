package com.giga.nexas.dto.clarias;

public interface ClariasParser<T extends Clarias> {

    String supportExtension();

    T parse(byte[] data, String filename, String charset);
}
