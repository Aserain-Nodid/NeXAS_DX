package com.giga.nexas.clarias;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.csv.*;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giga.nexas.dto.ResponseDTO;
import com.giga.nexas.dto.clarias.dat.Dat;
import com.giga.nexas.service.ClariasBinService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
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

@TestMethodOrder(OrderAnnotation.class)
@Execution(ExecutionMode.SAME_THREAD)
public class TestDat {

    private static final Logger log = LoggerFactory.getLogger(TestDat.class);
    private final ClariasBinService clariasBinService = new ClariasBinService();

    private static final Path GAME_DAT_DIR   = Paths.get("src/main/resources/game/clarias/dat");
    private static final Path JSON_OUTPUT_DIR = Paths.get("src/main/resources/datClariasJson");
    private static final Path DAT_OUTPUT_DIR  = Paths.get("src/main/resources/datClariasGenerated");
    private static final Path CSV_OUTPUT_DIR  = Paths.get("src/main/resources/datClariasCsvGenerated");

    private static final String DAT_EXT = ".dat";
    private static final String GENERATED_SUFFIX = ".generated";

    @Test
    @Order(1)
    void testGenerateDatJsonFiles() throws IOException {
        List<Dat> allDatList = new ArrayList<>();
        List<String> baseNames = new ArrayList<>();

        Files.createDirectories(JSON_OUTPUT_DIR);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(GAME_DAT_DIR, "*" + DAT_EXT)) {
            for (Path path : stream) {
                String fileName = path.getFileName().toString();
                String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
                baseNames.add(baseName);

                try {
                    ResponseDTO dto = clariasBinService.parse(path.toString(), "windows-31j");
                    Dat dat = (Dat) dto.getData();
                    allDatList.add(dat);
                } catch (Exception e) {
                    log.warn("❌ Failed to parse: {}", fileName, e);
                    throw e;
                }
            }
        }

        for (int i = 0; i < allDatList.size(); i++) {
            Dat dat = allDatList.get(i);
            String jsonStr = JSONUtil.toJsonStr(dat);
            Path jsonPath = JSON_OUTPUT_DIR.resolve(baseNames.get(i) + ".dat.json");
            FileUtil.writeUtf8String(jsonStr, jsonPath.toFile());
            log.info("✅ Exported: {}", jsonPath);
        }
    }

    @Test
    @Order(2)
    void testGenerateDatFilesByJson() throws IOException {
        Files.createDirectories(DAT_OUTPUT_DIR);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(JSON_OUTPUT_DIR, "*.json")) {
            for (Path path : stream) {
                String fileName = path.getFileName().toString();

                // 期望 JSON 文件名：<basename>.dat.json
                String baseName = fileName.endsWith(".dat.json")
                        ? fileName.substring(0, fileName.length() - ".dat.json".length())
                        : fileName.substring(0, fileName.lastIndexOf('.'));

                String jsonStr = FileUtil.readUtf8String(path.toFile());
                Dat dat = mapper.readValue(jsonStr, Dat.class);

                // 生成的二进制统一命名：<basename>.dat.generated
                Path output = DAT_OUTPUT_DIR.resolve(baseName + DAT_EXT + GENERATED_SUFFIX);
                clariasBinService.generate(output.toString(), dat, "windows-31j");
                log.info("✅ Generated: {}", output);
            }
        }
    }

    @Test
    @Order(3)
    void testDatParseGenerateBinaryConsistency() throws IOException {

        // 收集已生成文件：key = 原始名（<basename>.dat），value = 生成文件（<basename>.dat.generated）
        Map<String, Path> generatedMap = new HashMap<>();
        try (DirectoryStream<Path> genStream = Files.newDirectoryStream(DAT_OUTPUT_DIR, "*" + DAT_EXT + GENERATED_SUFFIX)) {
            for (Path gen : genStream) {

                // <basename>.dat.generated
                String genName = gen.getFileName().toString();

                // <basename>.dat
                String originalName = genName.replace(GENERATED_SUFFIX, "");
                generatedMap.put(originalName, gen);
            }
        }

        boolean anyIssue = false;
        int compared = 0;

        try (DirectoryStream<Path> oriStream = Files.newDirectoryStream(GAME_DAT_DIR, "*" + DAT_EXT)) {
            for (Path ori : oriStream) {

                // <basename>.dat
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
            if (Files.exists(DAT_OUTPUT_DIR)) {
                FileUtil.del(DAT_OUTPUT_DIR.toFile());
                log.info("🧹 Removed: {}", DAT_OUTPUT_DIR.toAbsolutePath());
            }
        } else {
            if (anyIssue) {
                Assertions.fail("Mismatch or missing generated files detected. Outputs retained for inspection.");
            } else {
                Assertions.fail("No files compared. Check inputs/outputs before cleanup.");
            }
        }
    }

    @Test
    void testToCsv() throws IOException {

        Files.createDirectories(CSV_OUTPUT_DIR);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(JSON_OUTPUT_DIR, "*.json")) {
            for (Path jsonPath : stream) {
                String baseName = jsonPath.getFileName().toString().replace(".json", "");

                String jsonStr = FileUtil.readUtf8String(jsonPath.toFile());
                Dat dat = mapper.readValue(jsonStr, Dat.class);

                Path csvPath = CSV_OUTPUT_DIR.resolve(baseName + ".csv");
                CsvWriter writer = CsvUtil.getWriter(csvPath.toFile(), CharsetUtil.CHARSET_UTF_8);

                // 第一行留空
                writer.write(new String[]{""});

                List<List<Object>> rows = dat.getData();
                for (List<Object> row : rows) {
                    String[] line = new String[row.size()];
                    for (int i = 0; i < row.size(); i++) {
                        Object v = row.get(i);
                        line[i] = (v == null) ? "" : String.valueOf(v);
                    }
                    writer.write(line);
                }

                writer.close();
                log.info("CSV exported: {}", csvPath);
            }
        }
    }

    @Test
    void testCsvPatchToJson() throws IOException {

        Files.createDirectories(JSON_OUTPUT_DIR);
        Files.createDirectories(CSV_OUTPUT_DIR);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(CSV_OUTPUT_DIR, "*.csv")) {
            for (Path csvPath : stream) {
                String baseName = csvPath.getFileName().toString().replace(".csv", "");
                Path jsonPath = JSON_OUTPUT_DIR.resolve(baseName + ".json");

                if (!Files.exists(jsonPath)) {
                    log.warn("JSON not found for CSV: {}", baseName);
                    continue;
                }

                String jsonStr = FileUtil.readUtf8String(jsonPath.toFile());
                Dat dat = mapper.readValue(jsonStr, Dat.class);
                List<List<Object>> data = dat.getData();

                CsvReader reader = CsvUtil.getReader();
                CsvData csvData = reader.read(FileUtil.getReader(csvPath.toFile(), CharsetUtil.CHARSET_UTF_8));
                List<CsvRow> csvRows = csvData.getRows();

                if (csvRows.isEmpty()) {
                    continue;
                }

                // 从第 2 行开始，对应 data 的第 0 行
                for (int csvRowIndex = 1; csvRowIndex < csvRows.size(); csvRowIndex++) {
                    int dataRowIndex = csvRowIndex - 1;
                    if (dataRowIndex >= data.size()) {
                        break;
                    }

                    CsvRow csvRow = csvRows.get(csvRowIndex);
                    List<Object> dataRow = data.get(dataRowIndex);

                    int colCount = Math.min(csvRow.size(), dataRow.size());
                    for (int col = 0; col < colCount; col++) {
                        String newVal = csvRow.get(col);
                        if (newVal == null || newVal.isEmpty()) {
                            // 空字符串表示不修改该单元格
                            continue;
                        }

                        Object oldVal = dataRow.get(col);
                        // 只覆盖原来是字符串或为 null 的单元格，避免把数值字段改成字符串
                        if (oldVal == null || oldVal instanceof String) {
                            dataRow.set(col, newVal);
                        }
                    }
                }

                String newJson = JSONUtil.toJsonStr(dat);
                FileUtil.writeUtf8String(newJson, jsonPath.toFile());
                log.info("Patched JSON from CSV: {}", jsonPath);
            }
        }
    }
}
