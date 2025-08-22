//package com.giga.nexas.bsdx;
//
//import com.giga.nexas.dto.ResponseDTO;
//import com.giga.nexas.dto.bsdx.dat.Dat;
//import com.giga.nexas.dto.bsdx.grp.Grp;
//import com.giga.nexas.dto.bsdx.grp.groupmap.*;
//import com.giga.nexas.service.BsdxBinService;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.nio.file.DirectoryStream;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class TestEditMekaSelectList {
//
//    private static final Logger log = LoggerFactory.getLogger(TestEditMekaSelectList.class);
//    private static final Path INPUT_DIR = Paths.get("src/main/resources/game/bsdx/dat");
//    private static final Path GRP_DIR = Paths.get("src/main/resources/game/bsdx/grp");
//    public static final Map<Integer, String> MEK_GRP  = new HashMap<>(1<<7,1);
//
//    private final BsdxBinService bsdxBinService = new BsdxBinService();
//
//    @Data
//    @AllArgsConstructor
//    private class SelectMekaMenu {
//        private String meka;
//        private Integer value2;
//        private Integer value3;
//    }
//
//    @Test
//    void testEditMekaSelectList() throws IOException {
//        register();
//        Map<String, Dat> datMap = getDat();
//        Dat selectMekaMenuDat = datMap.get("SelectMekaMenu");
//
//        List<SelectMekaMenu> SelectMekaMenuList = new ArrayList<>();
//
//        for (List<Object> datum : selectMekaMenuDat.getData()) {
//            Integer val1 = (Integer) datum.get(0);
//            Integer val2 = (Integer) datum.get(1);
//            Integer val3 = (Integer) datum.get(2);
//
//            SelectMekaMenu SelectMekaMenu = new SelectMekaMenu(MEK_GRP.get(val1), val2, val3);
//            SelectMekaMenuList.add(SelectMekaMenu);
//        }
//        System.out.println();
//    }
//
//    private Map<String, Dat> getDat() throws IOException {
//        Map<String, Dat> allDatMap = new HashMap<>();
//
//         try (DirectoryStream<Path> stream = Files.newDirectoryStream(INPUT_DIR, "*.dat")) {
//             for (Path path : stream) {
//                 String fileName = path.getFileName().toString();
//                 String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
//
//                 try {
//                     ResponseDTO dto = bsdxBinService.parse(path.toString(), "windows-31j");
//                     Dat dat = (Dat) dto.getData();
//                     allDatMap.put(baseName, dat);
//                 } catch (Exception e) {
//                     log.warn("❌ Failed to parse: {}", fileName, e);
//                 }
//             }
//         }
//         return allDatMap;
//    }
//
//    private void register() {
//        try (DirectoryStream<Path> stream = Files.newDirectoryStream(GRP_DIR, "*.grp")) {
//            for (Path path : stream) {
//                String fileName = path.getFileName().toString();
//
//                try {
//                    ResponseDTO<?> dto = bsdxBinService.parse(path.toString(), "windows-31j");
//                    Grp grp = (Grp) dto.getData();
//                    if (grp instanceof MekaGroupGrp) {
//                        registerMekaGroupGrp(grp);
//                    } else if (grp instanceof BatVoiceGrp) {
//                        registerBatVoiceGrp(grp);
//                    } else if (grp instanceof SeGroupGrp) {
//                        registerSeGroupGrp(grp);
//                    } else if (grp instanceof SpriteGroupGrp) {
//                        registerSpriteGroupGrp(grp);
//                    } else if (grp instanceof TermGrp) {
//                        registerTermGrp(grp);
//                    } else if (grp instanceof WazaGroupGrp) {
//                        registerWazaGroupGrp(grp);
//                    }
//                } catch (Exception e) {
//                    log.warn("Failed to parse: {}", fileName);
//                }
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private void registerMekaGroupGrp(Grp grp) {
//        MekaGroupGrp mekaGroupGrp = (MekaGroupGrp) grp;
//        for (int i = 0; i < mekaGroupGrp.getMekaList().size(); i++) {
//            MEK_GRP.put(i, mekaGroupGrp.getMekaList().get(i).getMekaName());
//        }
//        MEK_GRP.put(110, "Akao"); // 没有，所以手动注册
//    }
//
//    private void registerBatVoiceGrp(Grp grp) {
//        BatVoiceGrp batGroupGrp = (BatVoiceGrp) grp;
//        batGroupGrp.get
//    }
//
//}
