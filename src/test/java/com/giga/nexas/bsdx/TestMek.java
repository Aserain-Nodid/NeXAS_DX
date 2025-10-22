package com.giga.nexas.bsdx;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giga.nexas.dto.ResponseDTO;
import com.giga.nexas.dto.bsdx.mek.Mek;
import com.giga.nexas.service.BsdxBinService;
import org.junit.jupiter.api.*;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@TestMethodOrder(OrderAnnotation.class)
@Execution(ExecutionMode.SAME_THREAD)
public class TestMek {

    private static final Logger log = LoggerFactory.getLogger(TestMek.class);
    private final BsdxBinService bsdxBinService = new BsdxBinService();

    private static final Path GAME_MEK_DIR   = Paths.get("src/main/resources/game/bsdx/mek");
    private static final Path JSON_OUTPUT_DIR = Paths.get("src/main/resources/mekBsdxJson");
    private static final Path MEK_OUTPUT_DIR  = Paths.get("src/main/resources/mekBsdxGenerated");

    private static final String MEK_EXT = ".mek";
    private static final String GENERATED_SUFFIX = ".generated";

    @Test
    @Order(1)
    void testGenerateMekJsonFiles() throws IOException {
        List<Mek> allMekList = new ArrayList<>();
        List<String> baseNames = new ArrayList<>();

        Files.createDirectories(JSON_OUTPUT_DIR);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(GAME_MEK_DIR, "*" + MEK_EXT)) {
            for (Path path : stream) {
                String fileName = path.getFileName().toString();
                String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
                baseNames.add(baseName);

                try {
                    ResponseDTO dto = bsdxBinService.parse(path.toString(), "windows-31j");
                    Mek mek = (Mek) dto.getData();
                    allMekList.add(mek);
                } catch (Exception e) {
                    log.warn("❌ Failed to parse: {}", fileName, e);
                    throw e;
                }
            }
        }

        for (int i = 0; i < allMekList.size(); i++) {
            Mek mek = allMekList.get(i);
            String jsonStr = JSONUtil.toJsonStr(mek);
            Path jsonPath = JSON_OUTPUT_DIR.resolve(baseNames.get(i) + ".mek.json");
            FileUtil.writeUtf8String(jsonStr, jsonPath.toFile());
            log.info("✅ Exported: {}", jsonPath);
        }
    }

    @Test
    @Order(2)
    void testGenerateMekFilesByJson() throws IOException {
        Files.createDirectories(MEK_OUTPUT_DIR);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(JSON_OUTPUT_DIR, "*.json")) {
            for (Path path : stream) {
                String fileName = path.getFileName().toString();

                // 期望 JSON 文件名：<basename>.mek.json
                String baseName = fileName.endsWith(".mek.json")
                        ? fileName.substring(0, fileName.length() - ".mek.json".length())
                        : fileName.substring(0, fileName.lastIndexOf('.'));

                String jsonStr = FileUtil.readUtf8String(path.toFile());
                Mek mek = mapper.readValue(jsonStr, Mek.class);

                // 生成的二进制统一命名：<basename>.mek.generated
                Path output = MEK_OUTPUT_DIR.resolve(baseName + MEK_EXT + GENERATED_SUFFIX);
                bsdxBinService.generate(output.toString(), mek, "windows-31j");
                log.info("✅ Generated: {}", output);
            }
        }
    }

    @Test
    @Order(3)
    void testMekParseGenerateBinaryConsistency() throws IOException {

        // 收集已生成文件：key = 原始名（<basename>.mek），value = 生成文件（<basename>.mek.generated）
        Map<String, Path> generatedMap = new HashMap<>();
        try (DirectoryStream<Path> genStream = Files.newDirectoryStream(MEK_OUTPUT_DIR, "*" + MEK_EXT + GENERATED_SUFFIX)) {
            for (Path gen : genStream) {

                // <basename>.mek.generated
                String genName = gen.getFileName().toString();

                // <basename>.mek
                String originalName = genName.replace(GENERATED_SUFFIX, "");
                generatedMap.put(originalName, gen);
            }
        }

        boolean anyIssue = false;
        int compared = 0;

        try (DirectoryStream<Path> oriStream = Files.newDirectoryStream(GAME_MEK_DIR, "*" + MEK_EXT)) {
            for (Path ori : oriStream) {

                // <basename>.mek
                String name = ori.getFileName().toString();
                Path gen = generatedMap.get(name);
                if (gen == null) {
                    anyIssue = true;
                    log.error("❌ Generated file not found for original: {}", name);
                    continue;
                }

                byte[] originalBytes = FileUtil.readBytes(ori.toFile());
                byte[] generatedBytes = FileUtil.readBytes(gen.toFile());
                compared++;

                if (!ArrayUtil.equals(originalBytes, generatedBytes)) {
                    anyIssue = true;
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

        // 全部成功 => 清理；否则断言失败且不清理
        if (!anyIssue && compared > 0) {
            if (Files.exists(JSON_OUTPUT_DIR)) {
                FileUtil.del(JSON_OUTPUT_DIR.toFile());
                log.info("🧹 Removed: {}", JSON_OUTPUT_DIR.toAbsolutePath());
            }
            if (Files.exists(MEK_OUTPUT_DIR)) {
                FileUtil.del(MEK_OUTPUT_DIR.toFile());
                log.info("🧹 Removed: {}", MEK_OUTPUT_DIR.toAbsolutePath());
            }
        } else {
            if (anyIssue) {
                Assertions.fail("Mismatch or missing generated files detected. Outputs retained for inspection.");
            } else {
                Assertions.fail("No files compared. Check inputs/outputs before cleanup.");
            }
        }
    }
}
