package com.giga.nexas.bhe2bsdx;

import com.giga.nexas.bhe2bsdx.steps.TransMeka;
import com.giga.nexas.dto.ResponseDTO;

import com.giga.nexas.service.BheBinService;
import com.giga.nexas.service.BsdxBinService;
import com.giga.nexas.util.PacUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class TransferTest {

    private static final Path OUTPUT_DIR = Paths.get("src/main/resources/testBhe");

    private static final Logger log = LoggerFactory.getLogger(TransferTest.class);
    private final BsdxBinService bsdxBinService = new BsdxBinService();
    private final BheBinService bheBinService = new BheBinService();

    /**
     * 移植用，pipeline模拟
     */
    @Test
    public void testPipeline() throws Exception {

        String outputPath = "D:\\A\\NeXAS_DX\\src\\main\\resources\\testBhe";

        // 1.注册全部所需文件资源
        // grp
        Map<String, com.giga.nexas.dto.bsdx.grp.Grp> bsdxGrp = registerBsdxGrp();
        Map<String, com.giga.nexas.dto.bhe.grp.Grp> bheGrp = registerBheGrp();
        // mek
        Map<String, com.giga.nexas.dto.bsdx.mek.Mek> bsdxMek = registerBsdxMek();
        Map<String, com.giga.nexas.dto.bhe.mek.Mek> bheMek = registerBheMek();
        // waz
        Map<String, com.giga.nexas.dto.bsdx.waz.Waz> bsdxWaz = registerBsdxWaz();
        Map<String, com.giga.nexas.dto.bhe.waz.Waz> bheWaz = registerBheWaz();
        // spm
        Map<String, com.giga.nexas.dto.bsdx.spm.Spm> bsdxSpm = registerBsdxSpm();
        Map<String, com.giga.nexas.dto.bhe.spm.Spm> bheSpm = registerBheSpm();
        // dat

        // 2.抽出移植目标
        // 月读
        // batVoice
        com.giga.nexas.dto.bhe.grp.groupmap.BatVoiceGrp bheBatVoice =
                (com.giga.nexas.dto.bhe.grp.groupmap.BatVoiceGrp) bheGrp.get("batvoice");
        com.giga.nexas.dto.bsdx.grp.groupmap.BatVoiceGrp bsdxBatVoice =
                (com.giga.nexas.dto.bsdx.grp.groupmap.BatVoiceGrp) bsdxGrp.get("batvoice");
        com.giga.nexas.dto.bhe.grp.groupmap.BatVoiceGrp.BatVoiceGroup tsukuyomiBatvoice = bheBatVoice.getVoiceList().get(1);
        // mek
        com.giga.nexas.dto.bhe.mek.Mek tsukuyomiMek = bheMek.get("tsukuyomi");
        // waz
        com.giga.nexas.dto.bhe.waz.Waz tsukuyomiWaz = bheWaz.get("tsukuyomi");
        // spm
        com.giga.nexas.dto.bhe.spm.Spm tsukuyomiSpm = bheSpm.get("tsukuyomi");
        com.giga.nexas.dto.bhe.spm.Spm tsukuyomiCSpm = bheSpm.get("c_tsukuyomi");
        com.giga.nexas.dto.bhe.spm.Spm tsukuyomiSSpm = bheSpm.get("s_tsukuyomi");
        com.giga.nexas.dto.bhe.spm.Spm tsukuyomiGSpm = bheSpm.get("g_tsukuyomi");
        com.giga.nexas.dto.bhe.spm.Spm tsukuyomiMSpm = bheSpm.get("m_tsukuyomi");

        com.giga.nexas.dto.bsdx.spm.Spm mekaPilotSpm = bsdxSpm.get("mekapilot");
        com.giga.nexas.dto.bsdx.spm.Spm selectMekaMenuMekaSpm = bsdxSpm.get("selectmekamenumeka");

        //
        TransMeka.process(
                tsukuyomiMek,
                tsukuyomiWaz,
                tsukuyomiSpm,
                tsukuyomiCSpm,
                tsukuyomiSSpm,
                tsukuyomiGSpm,
                tsukuyomiMSpm,

                tsukuyomiBatvoice,
                bsdxBatVoice,

                mekaPilotSpm,
                selectMekaMenuMekaSpm);

        // 打包
        log.info("outputPath === {}", PacUtil.unpack(outputPath));
    }

    // grp
    private static final Path BSDX_GRP_DIR = Paths.get("src/main/resources/game/bsdx/grp");
    private static final Path BHE_GRP_DIR = Paths.get("src/main/resources/game/bhe/grp");
    private Map<String, com.giga.nexas.dto.bsdx.grp.Grp> registerBsdxGrp() throws IOException {
        Map<String, com.giga.nexas.dto.bsdx.grp.Grp> grpMap = new HashMap<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(BSDX_GRP_DIR, "*.grp")) {
            for (Path path : stream) {
                String fileName = path.getFileName().toString();
                String baseName = fileName.substring(0, fileName.lastIndexOf('.')).toLowerCase();

                try {
                    ResponseDTO<?> dto = bsdxBinService.parse(path.toString(), "windows-31j");
                    com.giga.nexas.dto.bsdx.grp.Grp grp = (com.giga.nexas.dto.bsdx.grp.Grp) dto.getData();
                    grpMap.put(baseName, grp);
                } catch (Exception e) {
                    log.warn("❌ Failed to parse bsdxGrp: {}", fileName);
                }
            }
        }

        return grpMap;
    }
    private Map<String, com.giga.nexas.dto.bhe.grp.Grp> registerBheGrp() throws IOException {
        Map<String, com.giga.nexas.dto.bhe.grp.Grp> grpMap = new HashMap<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(BHE_GRP_DIR, "*.grp")) {
            for (Path path : stream) {
                String fileName = path.getFileName().toString();
                String baseName = fileName.substring(0, fileName.lastIndexOf('.')).toLowerCase();

                try {
                    ResponseDTO<?> dto = bheBinService.parse(path.toString(), "windows-31j");
                    com.giga.nexas.dto.bhe.grp.Grp grp = (com.giga.nexas.dto.bhe.grp.Grp) dto.getData();
                    grpMap.put(baseName, grp);
                } catch (Exception e) {
                    log.warn("❌ Failed to parse bheGrp: {}", fileName);
                }
            }
        }

        return grpMap;
    }

    // mek
    private static final Path BSDX_MEK_DIR = Paths.get("src/main/resources/game/bsdx/mek");
    private static final Path BHE_MEK_DIR = Paths.get("src/main/resources/game/bhe/mek");
    private Map<String, com.giga.nexas.dto.bsdx.mek.Mek> registerBsdxMek() throws IOException {
        Map<String, com.giga.nexas.dto.bsdx.mek.Mek> mekMap = new HashMap<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(BSDX_MEK_DIR, "*.mek")) {
            for (Path path : stream) {
                String fileName = path.getFileName().toString();
                String baseName = fileName.substring(0, fileName.lastIndexOf('.')).toLowerCase();

                try {
                    ResponseDTO<?> dto = bsdxBinService.parse(path.toString(), "windows-31j");
                    com.giga.nexas.dto.bsdx.mek.Mek mek = (com.giga.nexas.dto.bsdx.mek.Mek) dto.getData();
                    mekMap.put(baseName, mek);
                } catch (Exception e) {
                    log.warn("❌ Failed to parse bsdxMek: {}", fileName);
                }
            }
        }

        return mekMap;
    }
    private Map<String, com.giga.nexas.dto.bhe.mek.Mek> registerBheMek() throws IOException {
        Map<String, com.giga.nexas.dto.bhe.mek.Mek> mekMap = new HashMap<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(BHE_MEK_DIR, "*.mek")) {
            for (Path path : stream) {
                String fileName = path.getFileName().toString();
                String baseName = fileName.substring(0, fileName.lastIndexOf('.')).toLowerCase();

                try {
                    ResponseDTO<?> dto = bheBinService.parse(path.toString(), "windows-31j");
                    com.giga.nexas.dto.bhe.mek.Mek mek = (com.giga.nexas.dto.bhe.mek.Mek) dto.getData();
                    mekMap.put(baseName, mek);
                } catch (Exception e) {
                    log.warn("❌ Failed to parse bheMek: {}", fileName);
                }
            }
        }

        return mekMap;
    }

    // waz
    private static final Path BSDX_WAZ_DIR = Paths.get("src/main/resources/game/bsdx/waz");
    private static final Path BHE_WAZ_DIR = Paths.get("src/main/resources/game/bhe/waz");
    private Map<String, com.giga.nexas.dto.bsdx.waz.Waz> registerBsdxWaz() throws IOException {
        Map<String, com.giga.nexas.dto.bsdx.waz.Waz> wazMap = new HashMap<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(BSDX_WAZ_DIR, "*.waz")) {
            for (Path path : stream) {
                String fileName = path.getFileName().toString();
                String baseName = fileName.substring(0, fileName.lastIndexOf('.')).toLowerCase();

                try {
                    ResponseDTO<?> dto = bsdxBinService.parse(path.toString(), "windows-31j");
                    com.giga.nexas.dto.bsdx.waz.Waz waz = (com.giga.nexas.dto.bsdx.waz.Waz) dto.getData();
                    wazMap.put(baseName, waz);
                } catch (Exception e) {
                    log.warn("❌ Failed to parse bsdxWaz: {}", fileName);
                }
            }
        }

        return wazMap;
    }
    private Map<String, com.giga.nexas.dto.bhe.waz.Waz> registerBheWaz() throws IOException {
        Map<String, com.giga.nexas.dto.bhe.waz.Waz> wazMap = new HashMap<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(BHE_WAZ_DIR, "*.waz")) {
            for (Path path : stream) {
                String fileName = path.getFileName().toString();
                String baseName = fileName.substring(0, fileName.lastIndexOf('.')).toLowerCase();

                try {
                    ResponseDTO<?> dto = bheBinService.parse(path.toString(), "windows-31j");
                    com.giga.nexas.dto.bhe.waz.Waz waz = (com.giga.nexas.dto.bhe.waz.Waz) dto.getData();
                    wazMap.put(baseName, waz);
                } catch (Exception e) {
                    log.warn("❌ Failed to parse bheWaz: {}", fileName);
                }
            }
        }

        return wazMap;
    }

    // spm
    // spm在多个版本中无差异，但已经确定2.0.0在bhe中多了关于hitbox的信息，变得更复杂了
    private static final Path BSDX_SPM_DIR = Paths.get("src/main/resources/game/bsdx/spm");
    private static final Path BHE_SPM_DIR = Paths.get("src/main/resources/game/bhe/spm");
    private Map<String, com.giga.nexas.dto.bsdx.spm.Spm> registerBsdxSpm() throws IOException {
        Map<String, com.giga.nexas.dto.bsdx.spm.Spm> spmMap = new HashMap<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(BSDX_SPM_DIR, "*.spm")) {
            for (Path path : stream) {
                String fileName = path.getFileName().toString();
                String baseName = fileName.substring(0, fileName.lastIndexOf('.')).toLowerCase();

                try {
                    ResponseDTO<?> dto = bsdxBinService.parse(path.toString(), "windows-31j");
                    com.giga.nexas.dto.bsdx.spm.Spm spm = (com.giga.nexas.dto.bsdx.spm.Spm) dto.getData();
                    spmMap.put(baseName, spm);
                } catch (Exception e) {
                    log.warn("❌ Failed to parse bsdxSpm: {}", fileName);
                }
            }
        }

        return spmMap;
    }
    private Map<String, com.giga.nexas.dto.bhe.spm.Spm> registerBheSpm() throws IOException {
        Map<String, com.giga.nexas.dto.bhe.spm.Spm> spmMap = new HashMap<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(BHE_SPM_DIR, "*.spm")) {
            for (Path path : stream) {
                String fileName = path.getFileName().toString();
                String baseName = fileName.substring(0, fileName.lastIndexOf('.')).toLowerCase();

                try {
                    ResponseDTO<?> dto = bheBinService.parse(path.toString(), "windows-31j");
                    com.giga.nexas.dto.bhe.spm.Spm spm = (com.giga.nexas.dto.bhe.spm.Spm) dto.getData();
                    spmMap.put(baseName, spm);
                } catch (Exception e) {
                    log.warn("❌ Failed to parse bheSpm: {}", fileName);
                }
            }
        }

        return spmMap;
    }

    // dat
    // dat无差别，全为csv

    @Test
    void outputClassName() {
        HashMap<String, Object> classMapBsdx = new HashMap<>();
        for (int i = 0; i < 72; i++) {
            com.giga.nexas.dto.bsdx.waz.wazfactory.wazinfoclass.obj.SkillInfoObject obj =
                    com.giga.nexas.dto.bsdx.waz.wazfactory.SkillInfoFactory.createEventObjectBsdx(i);
            log.info("{}", obj.getClass().getSimpleName());
            classMapBsdx.put(obj.getClass().getSimpleName(),null);
        }

        log.info("==========");

        for (int i = 0; i < 83; i++) {
            com.giga.nexas.dto.bhe.waz.wazfactory.wazinfoclass.obj.SkillInfoObject obj =
                    com.giga.nexas.dto.bhe.waz.wazfactory.SkillInfoFactory.createEventObjectBhe(i);
            log.info("{}", obj.getClass().getSimpleName());

        }

        log.info("==========");
        log.info("==========");
        log.info("==========");

        classMapBsdx.forEach((k, v) -> {log.info("{}", k);});

    }

}
