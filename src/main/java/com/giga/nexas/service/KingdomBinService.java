package com.giga.nexas.service;

import cn.hutool.core.util.StrUtil;
import com.giga.nexas.dto.ResponseDTO;
import com.giga.nexas.dto.kingdom.Kingdom;
import com.giga.nexas.dto.kingdom.KingdomParser;
import com.giga.nexas.dto.kingdom.tfn.parser.TfnParser;
import com.giga.nexas.exception.OperationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KingdomBinService {

    private final Map<String, KingdomParser<?>> parserMap = new HashMap<>();

    public KingdomBinService() {
        registerParser(new TfnParser());
    }

    public KingdomBinService(List<KingdomParser<?>> parsers) {
        for (KingdomParser<?> parser : parsers) {
            registerParser(parser);
        }
    }

    private void registerParser(KingdomParser<?> parser) {
        parserMap.put(parser.supportExtension().toLowerCase(), parser);
    }

    public ResponseDTO<?> parse(String path, String charset) throws IOException {
        String ext = getFileExtension(path);
        KingdomParser<?> parser = parserMap.get(ext);
        if (parser == null) {
            throw new OperationException(500, "unsupported file type for parsing: " + ext);
        }

        byte[] data = Files.readAllBytes(Paths.get(path));
        Kingdom parsed = parser.parse(data, getFileName(path), charset);
        parsed.setExtensionName(ext);
        return new ResponseDTO<>(parsed, "ok");
    }

    private String getFileExtension(String path) {
        int lastDotIndex = path.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new OperationException(500, "not an invalid file path!");
        }
        return path.substring(lastDotIndex + 1).toLowerCase();
    }

    private String getJsonExtension(Kingdom obj) {
        String ext = obj.getExtensionName();
        if (StrUtil.isEmpty(ext)) {
            throw new OperationException(500, "can't find extension name from JSON file!");
        }
        return ext.toLowerCase();
    }

    public static String getFileName(String path) {
        String fileName = Paths.get(path).getFileName().toString().toLowerCase();
        int extensionIndex = fileName.lastIndexOf(".");
        return extensionIndex > 0 ? fileName.substring(0, extensionIndex) : fileName;
    }

    public Map<String, KingdomParser<?>> getParserMap() {
        return new HashMap<>(parserMap);
    }

}
