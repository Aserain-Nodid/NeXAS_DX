package com.giga.nexas.service.engine.adapter;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giga.nexas.dto.ResponseDTO;
import com.giga.nexas.dto.kingdom.Kingdom;
import com.giga.nexas.dto.kingdom.tfn.Tfn;
import com.giga.nexas.exception.OperationException;
import com.giga.nexas.service.KingdomBinService;
import com.giga.nexas.service.engine.BinaryEngineAdapter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static com.giga.nexas.controller.consts.MainConst.DAT_EXT;

/**
 * Adapter for KINGDOM workflows.
 */
public class KingdomBinaryEngineAdapter implements BinaryEngineAdapter {

    private final KingdomBinService service = new KingdomBinService();
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public Set<String> supportedParseExtensions() {
        return service.getParserMap().keySet();
    }

    @Override
    public Set<String> supportedGenerateExtensions() {
        return null;
    }

    @Override
    public Path parse(Path inputFile, Path outputDir, String charset) throws IOException {
        ResponseDTO<?> response = service.parse(inputFile.toString(), charset);
        String json = JSONUtil.toJsonStr(response.getData());
        Files.createDirectories(outputDir);
        Path target = outputDir.resolve(inputFile.getFileName().toString() + ".json");
        FileUtil.writeUtf8String(json, target.toFile());
        return target;
    }

    @Override
    public Path generate(Path jsonFile, Path outputDir, String charset) throws IOException {
        
        return null;
    }

    private Kingdom mapPayload(String ext, String json) throws IOException {
        return switch (ext) {
            case DAT_EXT -> mapper.readValue(json, Tfn.class);
            default -> throw new OperationException(500, "unsupported KINGDOM extension: " + ext);
        };
    }
}
