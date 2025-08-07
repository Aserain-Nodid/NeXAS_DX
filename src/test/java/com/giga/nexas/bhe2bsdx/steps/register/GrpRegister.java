package com.giga.nexas.bhe2bsdx.steps.register;

import com.giga.nexas.dto.ResponseDTO;
import com.giga.nexas.dto.bsdx.grp.Grp;
import com.giga.nexas.dto.bsdx.grp.groupmap.MekaGroupGrp;
import com.giga.nexas.dto.bsdx.grp.groupmap.SpriteGroupGrp;
import com.giga.nexas.dto.bsdx.grp.groupmap.WazaGroupGrp;
import com.giga.nexas.service.BsdxBinService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class GrpRegister {

    private static final Logger log = LoggerFactory.getLogger(GrpRegister.class);

    private static final Path GRP_DIR = Paths.get("src/main/resources/game/bsdx/grp");
    private final BsdxBinService bsdxBinService = new BsdxBinService();
    public static final Map<Integer, String> MEK_GRP  = new HashMap<>(1<<7,1);
    public static final Map<Integer, String> WAZ_GRP  = new HashMap<>(1<<7,1);
    public static final Map<Integer, String> SPM_GRP  = new HashMap<>(1<<7,1);

    private void registerGrp() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(GRP_DIR, "*.grp")) {
            for (Path path : stream) {
                String fileName = path.getFileName().toString();

                try {
                    ResponseDTO<?> dto = bsdxBinService.parse(path.toString(), "windows-31j");
                    Grp grp = (Grp) dto.getData();
                    if (grp instanceof MekaGroupGrp) {
                        registerMek(grp);
                    } else if (grp instanceof WazaGroupGrp) {
                        registerWaz(grp);
                    } else if (grp instanceof SpriteGroupGrp) {
                        registerSpm(grp);
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse: {}", fileName);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerMek(Grp grp) {
        MekaGroupGrp mekaGroupGrp = (MekaGroupGrp) grp;
        for (int i = 0; i < mekaGroupGrp.getMekaList().size(); i++) {
            MEK_GRP.put(i, mekaGroupGrp.getMekaList().get(i).getMekaName());
        }
        MEK_GRP.put(110, "Akao"); // 没有，所以手动注册
    }

    private void registerWaz(Grp grp) {
        WazaGroupGrp wazGroupGrp = (WazaGroupGrp) grp;
        for (int i = 0; i < wazGroupGrp.getWazaList().size(); i++) {
            WAZ_GRP.put(i, wazGroupGrp.getWazaList().get(i).getWazaDisplayName());
        }
        WAZ_GRP.put(110, "Akao"); // 没有，所以手动注册
    }

    private void registerSpm(Grp grp) {
        SpriteGroupGrp spriteGroupGrp = (SpriteGroupGrp) grp;
        for (int i = 0; i < spriteGroupGrp.getSpriteList().size(); i++) {
            SPM_GRP.put(i, spriteGroupGrp.getSpriteList().get(i).getSpriteFileName().split("\\.")[0]);
        }
    }

}
