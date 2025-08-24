package com.giga.nexas.bsdx;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giga.nexas.dto.ResponseDTO;
import com.giga.nexas.dto.bsdx.mek.Mek;
import com.giga.nexas.service.BsdxBinService;
import com.giga.nexas.util.TransferUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestMek {

    private static final Logger log = LoggerFactory.getLogger(TestMek.class);

    private final BsdxBinService bsdxBinService = new BsdxBinService();

    private static final Path MEK_DIR = Paths.get("src/main/resources/game/bsdx/mek");
    private static final Path JSON_OUTPUT = Paths.get("src/main/resources/mekJson");
    private static final Path JSON_OUTPUT_DIR = Paths.get("src/main/resources/mekJson");
    private static final Path MEK_OUTPUT_DIR = Paths.get("src/main/resources/mekGenerated");

    @Test
    void testParseMek() throws IOException {
        List<Mek> allMek = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(MEK_DIR, "*.mek")) {
            for (Path path : stream) {
                try {
                    ResponseDTO dto = bsdxBinService.parse(path.toString(), "windows-31j");
                    allMek.add((Mek) dto.getData());
                } catch (Exception ignored) {
                    continue;
                }
            }
        }

        log.info("✅ Parsed mek count: {}", allMek.size());
    }

    @Test
    void testGenerateMekJsonFiles() throws IOException {
        Files.createDirectories(JSON_OUTPUT);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(MEK_DIR, "*.mek")) {
            for (Path path : stream) {
                try {
                    ResponseDTO dto = bsdxBinService.parse(path.toString(), "windows-31j");
                    Mek mek = (Mek) dto.getData();

                    String baseName = path.getFileName().toString().replace(".mek", "");
                    Path jsonPath = JSON_OUTPUT.resolve(baseName + ".mek.json");
                    FileUtil.writeUtf8String(JSONUtil.toJsonStr(mek), jsonPath.toFile());

                    log.info("✅ Exported: {}", jsonPath);
                } catch (Exception e) {
                    log.info("❌ Failed to parse: {}", path.getFileName());
                }
            }
        }
    }

    @Test
    void transJA2ZH() throws IOException {
        Path zhFile = null, jaFile = null;

        try (DirectoryStream<Path> zh = Files.newDirectoryStream(MEK_DIR, "*.zh.mek")) {
            for (Path path : zh) {
                zhFile = path;
                break;
            }
        }

        try (DirectoryStream<Path> ja = Files.newDirectoryStream(MEK_DIR, "*.ja.mek")) {
            for (Path path : ja) {
                jaFile = path;
                break;
            }
        }

        if (zhFile == null || jaFile == null) {
            log.error("❌ 找不到 .zh.mek 或 .ja.mek 文件");
            return;
        }

        Mek zhMek = (Mek) bsdxBinService.parse(zhFile.toString(), "GBK").getData();
        Mek jaMek = (Mek) bsdxBinService.parse(jaFile.toString(), "windows-31j").getData();

        TransferUtil.transJA2ZH(jaMek, zhMek);
        bsdxBinService.generate(zhFile.toString(), jaMek, "GBK");

        log.info("✅ 已用日语内容替换中文文件：{}", zhFile);
    }

    @Test
    void testGenerateMekFilesByJson() throws IOException {
        Files.createDirectories(MEK_OUTPUT_DIR);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(JSON_OUTPUT_DIR, "*.json")) {
            for (Path path : stream) {
                String jsonStr = FileUtil.readUtf8String(path.toFile());
                Mek mek = mapper.readValue(jsonStr, Mek.class);
                String baseName = path.getFileName().toString().replace(".json", "");
                Path output = MEK_OUTPUT_DIR.resolve(baseName);

                bsdxBinService.generate(output.toString(), mek, "windows-31j");
                log.info("✅ Generated: {}", output);
            }
        }
    }

    @Test
    void testMekParseGenerateBinaryConsistency() throws IOException {
        Map<String, Path> generatedMap = new HashMap<>();
        try (DirectoryStream<Path> genStream = Files.newDirectoryStream(MEK_OUTPUT_DIR, "*.mek")) {
            for (Path gen : genStream) {
                generatedMap.put(gen.getFileName().toString(), gen);
            }
        }

        try (DirectoryStream<Path> oriStream = Files.newDirectoryStream(MEK_DIR, "*.mek")) {
            for (Path ori : oriStream) {
                String name = ori.getFileName().toString();
                Path gen = generatedMap.get(name);
                if (gen == null) {
                    log.warn("⚠️ Not Found: {}", name);
                    continue;
                }

                byte[] originalBytes = FileUtil.readBytes(ori.toFile());
                byte[] generatedBytes = FileUtil.readBytes(gen.toFile());

                if (!ArrayUtil.equals(originalBytes, generatedBytes)) {
                    log.error("❌ Mismatch: {}", name);
                    int minLen = Math.min(originalBytes.length, generatedBytes.length);
                    for (int i = 0; i < minLen; i++) {
                        if (originalBytes[i] != generatedBytes[i]) {
                            log.error("Diff at 0x{}: orig=0x{} gen=0x{}",
                                    Integer.toHexString(i),
                                    Integer.toHexString(originalBytes[i] & 0xFF),
                                    Integer.toHexString(generatedBytes[i] & 0xFF));
                            break;
                        }
                    }
                } else {
                    log.info("✅ Match: {}", name);
                }
            }
        }
    }


}
