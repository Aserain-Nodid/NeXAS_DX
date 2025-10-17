package com.giga.nexas.controller;

import javafx.scene.control.TreeItem;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;

import static com.giga.nexas.controller.consts.MainConst.*;

@RequiredArgsConstructor
public class FilePickerController {

    private final MainViewController view;
    private boolean userSelectedOutput = false;
    private boolean lockOutput = false;

    // 全局路径变量
    private static final String PREF_INPUT_PATH = "lastInputPath";
    private static final String PREF_OUTPUT_PATH = "lastOutputPath";
    private final Preferences prefs = Preferences.userNodeForPackage(FilePickerController.class);

    public void setup() {

//        prefs.remove(PREF_INPUT_PATH);
//        prefs.remove(PREF_OUTPUT_PATH);

        // 初始化读取上次路径
        loadLastPaths();

        // 输入路径选择逻辑
        bindInputBrowse();

        // 输出路径选择逻辑
        bindOutputBrowse();

        // 绑定锁定输出路径复选框
        bindLockOutput();

        // 当输入路径以其它方式改变（例如拖放）时，也执行自动选择/自动填充逻辑
        view.getInputField().textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) return;

            // 保存输入路径偏好
            prefs.put(PREF_INPUT_PATH, newVal);

            File file = new File(newVal);

            // 自动选择功能（和原来一致）
            autoSelectFunction(file);

            // 只有在用户没有手动指定输出目录并且没有锁定时，才自动填充输出
            if (!userSelectedOutput && !lockOutput) {
                File parent = file.isDirectory() ? file : file.getParentFile();
                if (parent != null) {
                    String absolutePath = parent.getAbsolutePath();
                    view.getOutputField().setText(absolutePath);
                    prefs.put(PREF_OUTPUT_PATH, absolutePath);
                }
            }
        });
    }

    private void loadLastPaths() {
        String lastInput = prefs.get(PREF_INPUT_PATH, "");
        String lastOutput = prefs.get(PREF_OUTPUT_PATH, "");
        // 读取锁定状态
        lockOutput = prefs.getBoolean("lockOutputPath", false);
        view.getLockOutPutPath().setSelected(lockOutput);
        // 根据锁定状态设置输出控件是否可用
        view.getOutputField().setDisable(lockOutput);
        view.getOutputBrowse().setDisable(lockOutput);
        if (!lastInput.isEmpty()) {
            view.getInputField().setText(lastInput);
        }
        if (!lastOutput.isEmpty()) {
            view.getOutputField().setText(lastOutput);
        }

        // 如果lastInput存在，自动选择功能
        if (!lastInput.isEmpty()) {
            autoSelectFunction(new File(lastInput));
        }
    }

    private void bindInputBrowse() {
        view.getInputBrowse().setOnAction(e -> {
            Stage stage = (Stage) view.getRoot().getScene().getWindow();
            File selected;

            boolean isPack = false;
            var sel = view.getTree().getSelectionModel().getSelectedItem();
            if (sel != null && PAC.equals(sel.getValue())) {
                isPack = true;
            }

            if (isPack) {
                DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle("select a folder");

                File current = new File(view.getInputField().getText());
                if (current.exists() && current.isDirectory()) {
                    chooser.setInitialDirectory(current);
                }

                selected = chooser.showDialog(stage);
            } else {
                FileChooser chooser = new FileChooser();
                chooser.setTitle("select a file");

                File current = new File(view.getInputField().getText());
                if (current.exists()) {
                    chooser.setInitialDirectory(current.isDirectory() ? current : current.getParentFile());
                }

                selected = chooser.showOpenDialog(stage);
            }

            if (selected != null) {
                view.getInputField().setText(selected.getAbsolutePath());
                // 保存全局变量
                prefs.put(PREF_INPUT_PATH, selected.getAbsolutePath());

                // 手动选择时自动判断一个操作
                autoSelectFunction(selected);

                // 只有在用户没有手动指定输出目录时，才自动填充
                    if (!userSelectedOutput && !lockOutput) {
                    String absolutePath = selected.getParentFile().getAbsolutePath();
                    view.getOutputField().setText(absolutePath);
                    prefs.put(PREF_OUTPUT_PATH, absolutePath);
                }
            }
        });
    }

    private void bindOutputBrowse() {
        view.getOutputBrowse().setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("select output folder");

            File current = new File(view.getOutputField().getText());
            if (current.exists() && current.isDirectory()) {
                chooser.setInitialDirectory(current);
            }

            File selected = chooser.showDialog(view.getRoot().getScene().getWindow());
            if (selected != null) {
                    // 如果输出被锁定，则忽略用户选择
                    if (lockOutput) return;

                    view.getOutputField().setText(selected.getAbsolutePath());
                    prefs.put(PREF_OUTPUT_PATH, selected.getAbsolutePath());
                    userSelectedOutput = true;
            }
        });
    }

        private void bindLockOutput() {
            view.getLockOutPutPath().setOnAction(e -> {
                lockOutput = view.getLockOutPutPath().isSelected();
                prefs.putBoolean("lockOutputPath", lockOutput);

                // 禁用/启用输出路径输入与浏览按钮
                view.getOutputField().setDisable(lockOutput);
                view.getOutputBrowse().setDisable(lockOutput);
            });
        }

    private void autoSelectFunction(File file) {
        String path = file.getAbsolutePath();
        String autoFunc;

        if (file.isDirectory()) {
            autoFunc = PAC;
        } else if (path.toLowerCase().contains(".pac")) {
            autoFunc = UNPAC;
        } else if (path.toLowerCase().endsWith(".json")) {
            autoFunc = GENERATE;
        } else if (file.isFile()) {
            autoFunc = PARSE;
        } else {
            autoFunc = null;
        }

        // 如果匹配到了功能 自动选中对应子功能
        if (autoFunc != null) {
            TreeItem<String> item = findTreeItemByValue(view.getTree().getRoot(), autoFunc, new HashSet<>());
            if (item != null) {
                view.getTree().getSelectionModel().select(item);
            }
        }
    }

    private TreeItem<String> findTreeItemByValue(TreeItem<String> root, String value, Set<TreeItem<String>> visited) {
        if (root == null || visited.contains(root)) return null;
        visited.add(root);

        if (value.equals(root.getValue())) return root;
        for (TreeItem<String> child : root.getChildren()) {
            TreeItem<String> result = findTreeItemByValue(child, value, visited);
            if (result != null) return result;
        }
        return null;
    }
}
