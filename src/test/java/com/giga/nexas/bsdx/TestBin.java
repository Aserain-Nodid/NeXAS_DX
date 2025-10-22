package com.giga.nexas.bsdx;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.giga.nexas.dto.ResponseDTO;
import com.giga.nexas.dto.bsdx.bin.Bin;
import com.giga.nexas.service.BsdxBinService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
public class TestBin {

    private static final Logger log = LoggerFactory.getLogger(TestBin.class);
    private static final Path BIN_DIR = Paths.get("src/main/resources/game/bsdx/bin");
    private static final Path OUTPUT_DIR = Paths.get("src/main/resources/binJson");

    private final BsdxBinService bsdxBinService = new BsdxBinService();

    @Test
    @Order(1)
    public void testGenerateBinJsonFiles() throws IOException {
        if (!Files.exists(BIN_DIR)) {
            log.error("❌ Bin 目录不存在: {}", BIN_DIR);
            return;
        }

        Files.createDirectories(OUTPUT_DIR);
        List<Bin> allBinList = new ArrayList<>();
        List<String> baseNames = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(BIN_DIR, "*.bin")) {
            for (Path path : stream) {
                String fileName = URLDecoder.decode(path.getFileName().toString(), StandardCharsets.UTF_8);
                String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
                if (baseName.equalsIgnoreCase("__GLOBAL")) {
                    continue;
                }
                baseNames.add(baseName);

                try {
                    ResponseDTO parse = bsdxBinService.parse(path.toString(), "windows-31j");
                    Bin bin = (Bin) parse.getData();
                    if (bin != null) {
                        allBinList.add(bin);
                    } else {
                        log.info("⚠️ 解析为空: " + fileName);
                    }
                } catch (Exception e) {
                    log.error("❌ 解析失败: {} - {}", fileName, e.getMessage());
                    throw e;
                }
            }
        }

        log.info("✅ bin 文件总数: " + allBinList.size());

        for (int i = 0; i < allBinList.size(); i++) {
            Bin bin = allBinList.get(i);
            String jsonStr = JSONUtil.toJsonStr(bin);
            if (jsonStr == null) continue;

            Path outputPath = OUTPUT_DIR.resolve(baseNames.get(i) + ".json");
            FileUtil.writeUtf8String(jsonStr, outputPath.toFile());
            log.info("✅ 导出 JSON: {}", outputPath);
        }
    }

    @Test
    @Order(2)
    void testGenerateGlobalJsonFile() throws IOException {
        Path globalPath = BIN_DIR.resolve("__GLOBAL.bin");
        if (!Files.exists(globalPath)) {
            System.err.println("❌ 未找到 __GLOBAL.bin 文件: " + globalPath);
            return;
        }

        byte[] data = FileUtil.readBytes(globalPath.toFile());
        com.giga.nexas.dto.bsdx.bin.parser.GLOBALParser parser = new com.giga.nexas.dto.bsdx.bin.parser.GLOBALParser();
        com.giga.nexas.dto.bsdx.bin.GLOBAL global = parser.parse(data, "__GLOBAL", "windows-31j");

        String jsonStr = JSONUtil.toJsonStr(global);
        Path outputPath = OUTPUT_DIR.resolve("__GLOBAL.json");
        FileUtil.writeUtf8String(jsonStr, outputPath.toFile());

        System.out.println("✅ 导出 __GLOBAL.json: " + outputPath);
    }

    @Test
    @Order(3)
    void testBinParseGenerateBinaryConsistency() throws IOException {
        Path BIN_OUTPUT_DIR = Paths.get("src/main/resources/binGenerated");
        Map<String, Path> generatedMap = new HashMap<>();
        if (Files.exists(BIN_OUTPUT_DIR)) {
            try (DirectoryStream<Path> genStream = Files.newDirectoryStream(BIN_OUTPUT_DIR, "*.generated.bin")) {
                for (Path gen : genStream) {
                    generatedMap.put(gen.getFileName().toString(), gen);
                }
            }
        }

        Path mismatchDir = BIN_OUTPUT_DIR.resolve("mismatch");
        Files.createDirectories(mismatchDir);

        try (DirectoryStream<Path> oriStream = Files.newDirectoryStream(BIN_DIR, "*.bin")) {
            for (Path ori : oriStream) {
                String oriName = ori.getFileName().toString();
                if ("__GLOBAL.bin".equals(oriName)) {
                    log.info("Skip GLOBAL bin: {}", ori);
                    continue;
                }

                String genName = oriName.replace(".bin", ".generated.bin");
                Path gen = generatedMap.get(genName);
                if (gen == null) {
                    log.warn("Not Found generated file for: {}", oriName);
                    continue;
                }

                byte[] originalBytes = FileUtil.readBytes(ori.toFile());
                byte[] generatedBytes = FileUtil.readBytes(gen.toFile());

                if (!java.util.Arrays.equals(originalBytes, generatedBytes)) {
                    log.error("Mismatch: {}", genName);
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
                    if (originalBytes.length != generatedBytes.length) {
                        log.error("Length diff: orig={} gen={}", originalBytes.length, generatedBytes.length);
                    }
                    String newName = gen.getFileName().toString().replace(".generated", "");
                    Path target = mismatchDir.resolve(newName);
                    Files.move(gen, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    log.warn("Moved mismatch file to: {}", target);
                } else {
                    log.info("Matched: {}", genName);
                }
            }
        }
    }

    @Test
    @Disabled
    public void testOutputIR() throws IOException {
        if (!Files.exists(BIN_DIR)) {
            log.error("❌ Bin 目录不存在: {}", BIN_DIR);
            return;
        }

        Files.createDirectories(OUTPUT_DIR);
        List<Bin> allBinList = new ArrayList<>();
        List<String> baseNames = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(BIN_DIR, "*.bin")) {
            for (Path path : stream) {
                String fileName = URLDecoder.decode(path.getFileName().toString(), StandardCharsets.UTF_8);
                String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
                baseNames.add(baseName);

                try {
                    ResponseDTO parse = bsdxBinService.parse(path.toString(), "windows-31j");
                    Bin bin = (Bin) parse.getData();
                    if (bin != null) {
                        allBinList.add(bin);
                    } else {
                        log.info("⚠️ 解析为空: " + fileName);
                    }
                } catch (Exception e) {
                    log.error("❌ 解析失败: {} - {}", fileName, e.getMessage());
                }
            }
        }

        log.info("✅ bin 文件总数: " + allBinList.size());

        for (int i = 0; i < allBinList.size(); i++) {
            Bin bin = allBinList.get(i);

            String jsonStr = JSONUtil.toJsonStr(bin);
            if (jsonStr == null) continue;

            Path outputPath = OUTPUT_DIR.resolve(baseNames.get(i) + ".json");
            FileUtil.writeUtf8String(jsonStr, outputPath.toFile());
            log.info("✅ 导出 bin 内的IR: {}", outputPath);
        }
    }

    @Test
    @Disabled
    void testOutputInstructions() throws IOException {
        if (!Files.exists(BIN_DIR)) {
            log.error("❌ Bin 目录不存在: {}", BIN_DIR);
            return;
        }

        Files.createDirectories(OUTPUT_DIR);
        List<List<Bin.Instruction>> allInstructionList = new ArrayList<>();
        List<String> baseNames = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(BIN_DIR, "*.bin")) {
            for (Path path : stream) {
                String fileName = URLDecoder.decode(path.getFileName().toString(), StandardCharsets.UTF_8);
                String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
                baseNames.add(baseName);

                try {
                    ResponseDTO parse = bsdxBinService.parse(path.toString(), "windows-31j");
                    Bin bin = (Bin) parse.getData();
                    if (bin != null) {
                        allInstructionList.add(bin.getInstructions());
                    } else {
                        log.info("⚠️ 解析为空: " + fileName);
                    }
                } catch (Exception e) {
                    log.error("❌ 解析失败: {} - {}", fileName, e.getMessage());
                    throw e;
                }
            }
        }

        for (int i = 0; i < allInstructionList.size(); i++) {
            List<Bin.Instruction> instructions = allInstructionList.get(i);
            StringBuilder instructionStr = new StringBuilder();
            for (Bin.Instruction instruction : instructions) {
                String line = String.format("%-8s\t%-2d\t%s",
                        instruction.getOpcode(),
                        instruction.getParamCount(),
                        instruction.getNativeFunction() != null ? instruction.getNativeFunction() : "");
                instructionStr.append(line).append("\n");
            }
            if (instructionStr.isEmpty()) continue;

            Path outputPath = OUTPUT_DIR.resolve(baseNames.get(i) + ".txt");
            FileUtil.writeUtf8String(instructionStr.toString(), outputPath.toFile());
            log.info("✅ 导出 JSON: {}", outputPath);
        }
    }

    @Test
    @Disabled
    void testGenerateBinFilesByJson() throws IOException {
        Path BIN_OUTPUT_DIR = Paths.get("src/main/resources/binGenerated");
        Files.createDirectories(BIN_OUTPUT_DIR);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(OUTPUT_DIR, "*.json")) {
            for (Path path : stream) {
                String fileName = path.getFileName().toString();
                String baseName = fileName.substring(0, fileName.lastIndexOf('.'));

                if ("__GLOBAL".equals(baseName)) {
                    log.info("Skip GLOBAL json: {}", path);
                    continue;
                }

                String jsonStr = FileUtil.readUtf8String(path.toFile());
                com.giga.nexas.dto.bsdx.bin.Bin bin = JSONUtil.toBean(jsonStr, com.giga.nexas.dto.bsdx.bin.Bin.class);

                Path output = BIN_OUTPUT_DIR.resolve(baseName + ".generated.bin");
                String charset = (bin != null && bin.getCharset() != null) ? bin.getCharset() : "windows-31j";

                bsdxBinService.generate(output.toString(), bin, charset);
                log.info("Generated: {}", output);
            }
        }
    }
}
