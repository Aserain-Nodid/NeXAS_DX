package com.giga.nexas.service;

import cn.hutool.core.util.StrUtil;
import com.giga.nexas.dto.ResponseDTO;
import com.giga.nexas.dto.bsdx.Bsdx;
import com.giga.nexas.dto.bsdx.BsdxGenerator;
import com.giga.nexas.dto.bsdx.BsdxParser;
import com.giga.nexas.dto.bsdx.bin.generator.BinGenerator;
import com.giga.nexas.dto.bsdx.bin.parser.BinParser;
import com.giga.nexas.dto.bsdx.dat.generator.DatGenerator;
import com.giga.nexas.dto.bsdx.dat.parser.DatParser;
import com.giga.nexas.dto.bsdx.grp.generator.GrpGenerator;
import com.giga.nexas.dto.bsdx.grp.parser.GrpParser;
import com.giga.nexas.dto.bsdx.mek.generator.MekGenerator;
import com.giga.nexas.dto.bsdx.mek.parser.MekParser;
import com.giga.nexas.dto.bsdx.spm.generator.SpmGenerator;
import com.giga.nexas.dto.bsdx.spm.parser.SpmParser;
import com.giga.nexas.dto.bsdx.waz.generator.WazGenerator;
import com.giga.nexas.dto.bsdx.waz.parser.WazParser;
import com.giga.nexas.exception.OperationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BsdxBinService {

    private final Map<String, BsdxParser<?>> parserMap = new HashMap<>();
    private final Map<String, BsdxGenerator<?>> generatorMap = new HashMap<>();

    public BsdxBinService() {
        // 注册parser
        registerParser(new SpmParser());
        registerParser(new MekParser());
        registerParser(new WazParser());
        registerParser(new DatParser());
        registerParser(new BinParser());
        registerParser(new GrpParser());

        // 注册generator
        registerGenerator(new SpmGenerator());
        registerGenerator(new MekGenerator());
        registerGenerator(new WazGenerator());
        registerGenerator(new DatGenerator());
        registerGenerator(new GrpGenerator());
        registerGenerator(new BinGenerator());
    }

    public BsdxBinService(List<BsdxParser<?>> bsdxParsers) {
        for (BsdxParser<?> bsdxParser : bsdxParsers) {
            registerParser(bsdxParser);
        }
    }

    private void registerParser(BsdxParser<?> bsdxParser) {
        parserMap.put(bsdxParser.supportExtension().toLowerCase(), bsdxParser);
    }

    private void registerGenerator(BsdxGenerator<?> bsdxGenerator) {
        generatorMap.put(bsdxGenerator.supportExtension().toLowerCase(), bsdxGenerator);
    }

    public ResponseDTO<?> parse(String path, String charset) throws IOException {
        String ext = getFileExtension(path);
        BsdxParser<?> bsdxParser = parserMap.get(ext);
        if (bsdxParser == null) {
            throw new OperationException(500, "unsupported file type for parsing: " + ext);
        }

        byte[] data = Files.readAllBytes(Paths.get(path));
        Bsdx parsed = bsdxParser.parse(data, getFileName(path), charset);
        parsed.setExtensionName(ext);
        return new ResponseDTO<>(parsed, "ok");
    }

    public <T extends Bsdx> ResponseDTO<?> generate(String path, T obj, String charset) throws IOException {
        String ext = getJsonExtension(obj);
        BsdxGenerator<T> gen = (BsdxGenerator<T>) generatorMap.get(ext);
        if (gen == null) {
            throw new OperationException(500, "unsupported file type for generation: " + ext);
        }
        gen.generate(path, obj, charset);
        return new ResponseDTO<>(null, "ok");
    }

    private String getFileExtension(String path) {
        int lastDotIndex = path.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new OperationException(500, "not an invalid file path!");
        }
        return path.substring(lastDotIndex + 1).toLowerCase();
    }

    private String getJsonExtension(Bsdx obj) {
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

    public Map<String, BsdxParser<?>> getParserMap() {
        return new HashMap<>(parserMap);
    }

    public Map<String, BsdxGenerator<?>> getGeneratorMap() {
        return new HashMap<>(generatorMap);
    }
}
