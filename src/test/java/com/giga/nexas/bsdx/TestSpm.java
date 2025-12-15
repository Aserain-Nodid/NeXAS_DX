package com.giga.nexas.bsdx;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giga.nexas.dto.ResponseDTO;
import com.giga.nexas.dto.bsdx.spm.Spm;
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
public class TestSpm {

    private static final Logger log = LoggerFactory.getLogger(TestSpm.class);
    private final BsdxBinService bsdxBinService = new BsdxBinService();

    private static final String CHARSET = "windows-31j";

    private static final Path GAME_SPM_DIR   = Paths.get("src/main/resources/game/bsdx/spm");
    private static final Path JSON_OUTPUT_DIR = Paths.get("src/main/resources/spmBsdxJson");
    private static final Path SPM_OUTPUT_DIR  = Paths.get("src/main/resources/spmBsdxGenerated");

    private static final String SPM_EXT = ".spm";
    private static final String GENERATED_SUFFIX = ".generated"; // 所有生成的二进制都带这个后缀（放在扩展名后面）

    @Test
    @Order(1)
    void testGenerateSpmJsonFiles() throws IOException {
        List<Spm> allSpmList = new ArrayList<>();
        List<String> baseNames = new ArrayList<>();

        Files.createDirectories(JSON_OUTPUT_DIR);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(GAME_SPM_DIR, "*" + SPM_EXT)) {
            for (Path path : stream) {
                String fileName = path.getFileName().toString();
                String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
                baseNames.add(baseName);

                try {
                    ResponseDTO dto = bsdxBinService.parse(path.toString(), CHARSET);
                    Spm spm = (Spm) dto.getData();
                    allSpmList.add(spm);
                    log.info("✅ parsed: {}", fileName);
                } catch (Exception e) {
                    log.warn("❌ Failed to parse: {}", fileName, e);
//                    throw e;
                }
            }
        }

        for (int i = 0; i < allSpmList.size(); i++) {
            Spm spm = allSpmList.get(i);
            String jsonStr = JSONUtil.toJsonStr(spm);
            Path jsonPath = JSON_OUTPUT_DIR.resolve(baseNames.get(i) + SPM_EXT + ".json");
            FileUtil.writeUtf8String(jsonStr, jsonPath.toFile());
            log.info("✅ Exported: {}", jsonPath);
        }
    }

    @Test
    @Order(2)
    void testGenerateSpmFilesByJson() throws IOException {
        Files.createDirectories(SPM_OUTPUT_DIR);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(JSON_OUTPUT_DIR, "*.json")) {
            for (Path path : stream) {
                String fileName = path.getFileName().toString();
                // 期望 JSON 文件名：<basename>.spm.json
                String baseName = fileName.endsWith(SPM_EXT + ".json")
                        ? fileName.substring(0, fileName.length() - (SPM_EXT + ".json").length())
                        : fileName.substring(0, fileName.lastIndexOf('.'));

                String jsonStr = FileUtil.readUtf8String(path.toFile());
                Spm spm = mapper.readValue(jsonStr, Spm.class);

                // 生成的二进制统一命名：<basename>.spm.generated
                Path output = SPM_OUTPUT_DIR.resolve(baseName + SPM_EXT + GENERATED_SUFFIX);
                bsdxBinService.generate(output.toString(), spm, CHARSET);
                log.info("✅ Generated: {}", output);
            }
        }
    }

    @Test
    @Order(3)
    void testSpmParseGenerateBinaryConsistency() throws IOException {
        Map<String, Path> generatedMap = new HashMap<>();
        try (DirectoryStream<Path> genStream = Files.newDirectoryStream(SPM_OUTPUT_DIR, "*" + SPM_EXT + GENERATED_SUFFIX)) {
            for (Path gen : genStream) {
                String genName = gen.getFileName().toString();
                String originalName = genName.replace(GENERATED_SUFFIX, "");
                generatedMap.put(originalName, gen);
            }
        }

        boolean anyIssue = false;
        int compared = 0;
        int matched = 0;

        try (DirectoryStream<Path> oriStream = Files.newDirectoryStream(GAME_SPM_DIR, "*" + SPM_EXT)) {
            for (Path ori : oriStream) {
                compared++;
                String name = ori.getFileName().toString();
                Path gen = generatedMap.get(name);
                if (gen == null) {
                    anyIssue = true;
                    log.error("❌ Generated file not found for original: {}", name);
                    continue;
                }

                byte[] originalBytes = FileUtil.readBytes(ori.toFile());
                byte[] generatedBytes = FileUtil.readBytes(gen.toFile());

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
                    matched++;
                    log.info("✅ Match: {}", name);
                }
            }
        }

        double rate = compared > 0 ? (matched * 1.0 / compared) : 0.0;
        log.info("Matched {}/{} ({})", matched, compared, String.format("%.2f%%", rate * 100.0));

        if (compared > 0 && rate >= 0.90) {
            if (Files.exists(JSON_OUTPUT_DIR)) {
                FileUtil.del(JSON_OUTPUT_DIR.toFile());
                log.info("🧹 Removed: {}", JSON_OUTPUT_DIR.toAbsolutePath());
            }
            if (Files.exists(SPM_OUTPUT_DIR)) {
                FileUtil.del(SPM_OUTPUT_DIR.toFile());
                log.info("🧹 Removed: {}", SPM_OUTPUT_DIR.toAbsolutePath());
            }
        } else {
            if (anyIssue) {
                Assertions.fail("Success rate below 90%. Outputs retained for inspection.");
            } else {
                Assertions.fail("No files compared. Check inputs/outputs before cleanup.");
            }
        }
    }
}
