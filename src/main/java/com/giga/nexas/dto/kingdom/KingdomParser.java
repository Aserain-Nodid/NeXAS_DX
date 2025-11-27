package com.giga.nexas.dto.kingdom;

public interface KingdomParser <T extends Kingdom>{

    String supportExtension();

    T parse(byte[] data, String filename, String charset);

}
