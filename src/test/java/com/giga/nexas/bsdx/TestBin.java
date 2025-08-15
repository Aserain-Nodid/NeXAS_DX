package com.giga.nexas.bsdx;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.giga.nexas.dto.ResponseDTO;
import com.giga.nexas.dto.bsdx.bin.Bin;
import com.giga.nexas.service.BsdxBinService;
import org.junit.jupiter.api.Test;
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

public class TestBin {

    private static final Logger log = LoggerFactory.getLogger(TestBin.class);
    private static final Path BIN_DIR = Paths.get("src/main/resources/game/bsdx/bin");
    private static final Path OUTPUT_DIR = Paths.get("src/main/resources/binJson");

    private final BsdxBinService bsdxBinService = new BsdxBinService();

    @Test
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
    void testOutputInstructions() throws IOException {
        {
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
    }

    @Test
    void testGenerateBinFilesByJson() throws IOException {
        // JSON 输入目录沿用 TestBin 中的 OUTPUT_DIR（src/main/resources/binJson）
        Path BIN_OUTPUT_DIR = Paths.get("src/main/resources/binGenerated");
        Files.createDirectories(BIN_OUTPUT_DIR);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(OUTPUT_DIR, "*.json")) {
            for (Path path : stream) {
                String fileName = path.getFileName().toString();
                String baseName = fileName.substring(0, fileName.lastIndexOf('.'));

                // 跳过 __GLOBAL.json（它有单独的 GLOBAL 解析/生成链路）
                if ("__GLOBAL".equals(baseName)) {
                    log.info("Skip GLOBAL json: {}", path);
                    continue;
                }

                String jsonStr = FileUtil.readUtf8String(path.toFile());
                com.giga.nexas.dto.bsdx.bin.Bin bin = JSONUtil.toBean(jsonStr, com.giga.nexas.dto.bsdx.bin.Bin.class);

                // 生成到 binGenerated 目录，文件名：<base>.generated.bin
                Path output = BIN_OUTPUT_DIR.resolve(baseName + ".generated.bin");
                String charset = (bin != null && bin.getCharset() != null) ? bin.getCharset() : "windows-31j";

                bsdxBinService.generate(output.toString(), bin, charset);
                log.info("Generated: {}", output);
            }
        }
    }

    @Test
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

                // 跳过 __GLOBAL.bin（它有独立的 GLOBAL 生成链路与测试）
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
                    // 长度差异提示
                    if (originalBytes.length != generatedBytes.length) {
                        log.error("Length diff: orig={} gen={}", originalBytes.length, generatedBytes.length);
                    }

                    // 移动 mismatch 文件
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


}
