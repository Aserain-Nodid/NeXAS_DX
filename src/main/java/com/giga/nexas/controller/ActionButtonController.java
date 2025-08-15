package com.giga.nexas.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giga.nexas.dto.ResponseDTO;
import com.giga.nexas.dto.bsdx.Bsdx;
import com.giga.nexas.dto.bsdx.bin.Bin;
import com.giga.nexas.dto.bsdx.grp.Grp;
import com.giga.nexas.dto.bsdx.mek.Mek;
import com.giga.nexas.dto.bsdx.spm.Spm;
import com.giga.nexas.dto.bsdx.waz.Waz;
import com.giga.nexas.exception.OperationException;
import com.giga.nexas.service.BsdxBinService;
import com.giga.nexas.service.PacService;
import javafx.scene.control.TreeItem;
import lombok.RequiredArgsConstructor;

import java.io.File;

import static com.giga.nexas.controller.consts.MainConst.*;

@RequiredArgsConstructor
public class ActionButtonController {

    private final MainViewController view;

    public void bind() {
        view.getActionButton().setOnAction(e -> {
            File file = new File(view.getInputField().getText());
            if (!file.exists()) {
                view.getLogArea().appendText("⚠ invalid path\n");
                return;
            }

            TreeItem<String> selected = view.getTree()
                    .getSelectionModel()
                    .getSelectedItem();
            if (selected == null || selected.getParent() == null) {
                view.getLogArea().appendText("⚠ please select an option！\n");
                return;
            }
            String func = selected.getValue();

            // 根据子功能名称调用对应方法
            switch (func) {
                case UNPAC -> unPac(file);
                case PAC -> pac(file);
                case PARSE -> parse(file);
                case GENERATE -> generate(file);
                default     -> view.getLogArea()
                        .appendText("⚠ unknown option:\n " + func + "\n");
            }
        });
    }

    private void parse(File selectedFile) {
        try {
            BsdxBinService service = new BsdxBinService();
            Object result = service.parse(selectedFile.getAbsolutePath(), "windows-31j").getData();
            String json = JSONUtil.toJsonStr(result);
            File outputFile = new File(view.getOutputField().getText(), FileUtil.getName(selectedFile) + ".json");
            FileUtil.writeUtf8String(json, outputFile);
            view.getLogArea().appendText("✔ parsing succeeded: \n" + selectedFile.getName() + "\n");
            view.getLogArea().appendText("✔ JSON file written to:\n" + outputFile.getAbsolutePath() + "\n");
        } catch (Exception e) {
            view.getLogArea().appendText("⚠ parsing failed: \n" + e.getMessage() + "\n");
        }
    }

    private void generate(File selectedFile) {
        try {
            String jsonStr = FileUtil.readUtf8String(selectedFile);
            ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            Bsdx obj;
            String ext = objectMapper.readValue(jsonStr, Bsdx.class).getExtensionName().toLowerCase();
            obj = switch (ext) {
                case WAZ_EXT -> objectMapper.readValue(jsonStr, Waz.class);
                case MEK_EXT -> objectMapper.readValue(jsonStr, Mek.class);
                case SPM_EXT -> objectMapper.readValue(jsonStr, Spm.class);
                case GRP_EXT -> objectMapper.readValue(jsonStr, Grp.class);
                case BIN_EXT -> objectMapper.readValue(jsonStr, Bin.class);
                default -> throw new OperationException(500, "unsupported extension: \n" + ext);
            };

            File outputPath = new File(view.getOutputField().getText(), FileUtil.mainName(selectedFile));
            new BsdxBinService().generate(outputPath.getAbsolutePath(), obj, "windows-31j");

            view.getLogArea().appendText("✔ binary game file generated:\n" + outputPath.getAbsolutePath() + "\n");
        } catch (Exception ex) {
            view.getLogArea().appendText("⚠ generation failed: \n" + ex.getMessage() + "\n");
            ex.printStackTrace();
        }
    }

    private void unPac(File input) {
        try {
            ResponseDTO result = new PacService().unPac(input.getAbsolutePath());
            view.getLogArea().appendText("✔ unpack log:\n" + result.getMsg() + "\n");
        } catch (Exception e) {
            view.getLogArea().appendText("⚠ unpacking failed: \n" + e + "\n");
            e.printStackTrace();
        }
    }

    private void pac(File input) {
        try {
            ResponseDTO result = new PacService().pac(input.getAbsolutePath(), "4");
            view.getLogArea().appendText("✔ pack log:\n" + result.getMsg() + "\n");
        } catch (Exception e) {
            view.getLogArea().appendText("⚠ packing failed: \n" + e + "\n");
            e.printStackTrace();
        }
    }
}
