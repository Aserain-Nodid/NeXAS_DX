package com.giga.nexas.kingdom;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giga.nexas.dto.ResponseDTO;
import com.giga.nexas.dto.kingdom.tfn.TGLFontFile2;
import com.giga.nexas.dto.kingdom.tfn.Tfn;
import com.giga.nexas.service.KingdomBinService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestTfn {

    private static final Logger log = LoggerFactory.getLogger(TestTfn.class);
    private final KingdomBinService kingdomBinService = new KingdomBinService();

    private static final String CHARSET = "windows-31j";

    private static final Path GAME_TFN_DIR = Paths.get("src/main/resources/game/kingdom/tfn");
    private static final Path JSON_OUTPUT_DIR = Paths.get("src/main/resources/tfnKingdomJson");
    private static final Path TFN_OUTPUT_DIR = Paths.get("src/main/resources/tfnKingdomGenerated");

    @Test
    void testGenerateTfnJsonFiles() throws IOException {
        List<Tfn> allTfnList = new ArrayList<>();
        List<String> baseNames = new ArrayList<>();

        Files.createDirectories(JSON_OUTPUT_DIR);

        int counter = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(GAME_TFN_DIR, "*.tfn")) {
            for (Path path : stream) {
                String fileName = path.getFileName().toString();
                String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
                baseNames.add(baseName);

                try {
                    ResponseDTO dto = kingdomBinService.parse(path.toString(), CHARSET);
                    Tfn tfn = (Tfn) dto.getData();
                    allTfnList.add(tfn);
                    log.info("✅ passed: {}", fileName);
                } catch (Exception e) {
                    log.warn("❌ Failed to parse: {}", fileName);
                    log.error(e.getMessage());
                    counter++;
                }
            }
        }

        for (int i = 0; i < allTfnList.size(); i++) {
            Tfn tfn = allTfnList.get(i);
            String jsonStr = JSONUtil.toJsonStr(tfn);
            Path jsonPath = JSON_OUTPUT_DIR.resolve(baseNames.get(i) + ".tfn.json");
            FileUtil.writeUtf8String(jsonStr, jsonPath.toFile());
            log.info("Exported: {}", jsonPath);
        }

        if (counter==0) {
            log.info("✅✅✅ All passed!!!");
        }
    }

    @Test
    void testGenerateTfnFilesByJson() throws IOException {
        Files.createDirectories(TFN_OUTPUT_DIR);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(JSON_OUTPUT_DIR, "*.json")) {
            for (Path path : stream) {
                String jsonStr = FileUtil.readUtf8String(path.toFile());
                Tfn tfn = mapper.readValue(jsonStr, Tfn.class);
                String baseName = path.getFileName().toString().replace(".tfn.json", "");
                Path output = TFN_OUTPUT_DIR.resolve(baseName + ".generated.tfn");

//                kingdomBinService.generate(output.toString(), tfn, CHARSET);
                log.info("✅ Generated: {}", output);
            }
        }
    }

    @Test
    void testTfnParseGenerateBinaryConsistency() throws IOException {
        Map<String, Path> generatedMap = new HashMap<>();
        try (DirectoryStream<Path> genStream = Files.newDirectoryStream(TFN_OUTPUT_DIR, "*.generated.tfn")) {
            for (Path gen : genStream) {
                generatedMap.put(gen.getFileName().toString(), gen);
            }
        }

        Path mismatchDir = TFN_OUTPUT_DIR.resolve("mismatch");
        Files.createDirectories(mismatchDir); // 确保 mismatch 文件夹存在

        int counter=0;
        try (DirectoryStream<Path> oriStream = Files.newDirectoryStream(GAME_TFN_DIR, "*.tfn")) {
            for (Path ori : oriStream) {
                String name = ori.getFileName().toString().replace(".tfn", ".generated.tfn");
                Path gen = generatedMap.get(name);
                if (gen == null) {
                    log.warn("Not Found: {}", name);
                    continue;
                }

                byte[] originalBytes = FileUtil.readBytes(ori.toFile());
                byte[] generatedBytes = FileUtil.readBytes(gen.toFile());

                if (!ArrayUtil.equals(originalBytes, generatedBytes)) {
                    counter++;
                    log.error("❌Mismatch: {}", name);
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

                    // 移动 mismatch 文件
                    String newName = gen.getFileName().toString();
                    Path target = mismatchDir.resolve(newName);
                    Files.move(gen, target, StandardCopyOption.REPLACE_EXISTING);
                    log.warn("Moved mismatch file to: {}", target);

                } else {
                    log.info("✅ Match: {}", name);
                }
            }
        }

        if (counter==0) {
            log.info("✅✅✅ All Matched!!!");
        }

    }

    private static final Path IMAGE_OUTPUT_DIR = Paths.get("src/main/resources/tfnImages");

    @Test
    void testExportToImageAtlas() throws IOException {
        Files.createDirectories(IMAGE_OUTPUT_DIR);
        int counter = 0;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(GAME_TFN_DIR, "*.tfn")) {
            for (Path path : stream) {
                String fileName = path.getFileName().toString();
                try {
                    // 1. 解析
                    ResponseDTO dto = kingdomBinService.parse(path.toString(), CHARSET);
                    Tfn tfn = (Tfn) dto.getData();

                    // 2. 导出
                    String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
                    Path outPath = IMAGE_OUTPUT_DIR.resolve(baseName + ".png");

                    generateAtlasImage(tfn, outPath.toFile());

                    log.info("🖼️ Exported Atlas: {}", outPath);

                } catch (Exception e) {
                    log.error("Failed to export image: {}", fileName, e);
                    counter++;
                }
            }
        }

        if (counter == 0) {
            log.info("✅✅✅ All Images Exported!!!");
        }
    }

    /**
     * 生成拼接大图的核心逻辑
     */
    private void generateAtlasImage(Tfn tfn, File outputFile) throws IOException {
        // 1. 收集所有字形数据，保持顺序：ASCII -> Kana -> Kanji A -> Kanji B
        // 为了方便绘制，我们需要记录每个块的数据类型（因为解码方式不同）
        List<GlyphTask> allTasks = new ArrayList<>();

        // ASCII (V1: 1bpp, V2: 2bpp)
        for (byte[] data : tfn.getAsciiGlyphs()) {
            allTasks.add(new GlyphTask(data, GlyphType.ASCII_KANA));
        }
        // Kana (V1: 1bpp, V2: 2bpp)
        for (byte[] data : tfn.getKanaGlyphs()) {
            allTasks.add(new GlyphTask(data, GlyphType.ASCII_KANA));
        }
        // Kanji A (V1: 1bpp, V2: 4bpp)
        for (byte[] data : tfn.getKanjiGlyphsA().values()) {
            allTasks.add(new GlyphTask(data, GlyphType.KANJI));
        }
        // Kanji B (V1: 1bpp, V2: 4bpp)
        for (byte[] data : tfn.getKanjiGlyphsB().values()) {
            allTasks.add(new GlyphTask(data, GlyphType.KANJI));
        }

        if (allTasks.isEmpty()) return;

        // 2. 计算画布大小
        int charW = tfn.getWidth();
        int charH = tfn.getHeight();
        int totalGlyphs = allTasks.size();

        // 设定一行显示多少个字，例如 64 个
        int columns = 64;
        int rows = (int) Math.ceil((double) totalGlyphs / columns);

        int imgWidth = columns * charW;
        int imgHeight = rows * charH;

        // 3. 创建画布 (使用 ARGB 支持透明)
        BufferedImage image = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);

        // 4. 绘制
        boolean isV2 = (tfn instanceof TGLFontFile2);

        for (int i = 0; i < totalGlyphs; i++) {
            GlyphTask task = allTasks.get(i);

            // 计算当前字形在画布上的左上角坐标
            int col = i % columns;
            int row = i / columns;
            int startX = col * charW;
            int startY = row * charH;

            // 根据版本和类型调用不同的渲染器
            if (!isV2) {
                // TGLFontFile (V1): 全是 1bpp
                draw1bpp(image, task.data, startX, startY, charW, charH);
            } else {
                // TGLFontFile2 (V2): 混合位深
                if (task.type == GlyphType.ASCII_KANA) {
                    draw2bpp(image, task.data, startX, startY, charW, charH);
                } else {
                    draw4bpp(image, task.data, startX, startY, charW, charH);
                }
            }
        }

        // 5. 保存
        ImageIO.write(image, "png", outputFile);
    }

    // --- 渲染辅助方法 ---

    private void draw1bpp(BufferedImage img, byte[] data, int startX, int startY, int w, int h) {
        // V1 Stride 计算: (w / 8) + ((w & 0xF) != 0 ? 1 : 0)
        int stride = (w / 8) + ((w & 0xF) != 0 ? 1 : 0);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int byteIndex = y * stride + (x / 8);
                if (byteIndex >= data.length) break;

                int bitIndex = 7 - (x % 8);
                int bit = (data[byteIndex] >> bitIndex) & 1;

                if (bit == 1) {
                    img.setRGB(startX + x, startY + y, 0xFFFFFFFF); // 白色不透明
                }
                // 0 则是透明，BufferedImage 默认初始化就是透明，不用处理
            }
        }
    }

    private void draw2bpp(BufferedImage img, byte[] data, int startX, int startY, int w, int h) {
        // V2 ASCII Stride: ceil(w / 4)
        int stride = (w + 3) / 4;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int byteIndex = y * stride + (x / 4);
                if (byteIndex >= data.length) break;

                int shift = 6 - (x % 4) * 2;
                int val = (data[byteIndex] >> shift) & 0x03;

                if (val > 0) {
                    int alpha = val * 85; // 0->0, 1->85, 2->170, 3->255
                    int color = (alpha << 24) | 0x00FFFFFF; // 白色 + Alpha
                    img.setRGB(startX + x, startY + y, color);
                }
            }
        }
    }

    private void draw4bpp(BufferedImage img, byte[] data, int startX, int startY, int w, int h) {
        // V2 Kanji Stride: ceil(w / 2)
        int stride = (w + 1) / 2;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int byteIndex = y * stride + (x / 2);
                if (byteIndex >= data.length) break;

                int val;
                if (x % 2 == 0) val = (data[byteIndex] >> 4) & 0x0F;
                else val = data[byteIndex] & 0x0F;

                if (val > 0) {
                    int alpha = val * 17; // 0-15 -> 0-255
                    int color = (alpha << 24) | 0x00FFFFFF; // 白色 + Alpha
                    img.setRGB(startX + x, startY + y, color);
                }
            }
        }
    }

    // --- 内部辅助类 ---

    private enum GlyphType {
        ASCII_KANA,
        KANJI
    }

    private static class GlyphTask {
        byte[] data;
        GlyphType type;

        public GlyphTask(byte[] data, GlyphType type) {
            this.data = data;
            this.type = type;
        }
    }

}
