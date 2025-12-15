package com.giga.nexas.controller;

import com.giga.nexas.controller.model.EngineType;
import com.giga.nexas.controller.model.WorkspaceCategory;
import com.giga.nexas.controller.model.WorkspaceState;
import com.giga.nexas.controller.support.DirectoryScanner;
import javafx.application.Platform;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * 负责目录选择与扫描逻辑的控制器。
 */
@RequiredArgsConstructor
public class FilePickerController {

    private static final String PREF_INPUT_PATH = "lastInputPath";
    private static final String PREF_OUTPUT_PATH = "lastOutputPath";
    private static final String PREF_LOCK_OUTPUT = "lockOutputPath";

    private final MainViewController view;
    private final WorkspaceState state;
    private final DirectoryScanner scanner;

    private final Preferences prefs = Preferences.userNodeForPackage(FilePickerController.class);

    private boolean userSelectedOutput = false;
    private boolean lockOutput = false;

    public void setup() {
        loadPreferences();
        bindInputBrowse();
        bindOutputBrowse();
        bindLockToggle();
        bindManualPathInput();
        bindStateRescan();
        bindReloadButton();
    }

    public void loadDirectory(Path directory) {
        if (directory == null) {
            return;
        }
        if (!Files.isDirectory(directory)) {
            appendLog("Input path is not a directory: " + directory);
            return;
        }
        view.getInputField().setText(directory.toString());
        prefs.put(PREF_INPUT_PATH, directory.toString());
        state.getInputDirectory().set(directory);

        if (!userSelectedOutput && !lockOutput) {
            updateOutputPath(directory);
        }

        scanWorkspace(directory);
    }

    private void loadPreferences() {
        String lastInput = prefs.get(PREF_INPUT_PATH, "");
        String lastOutput = prefs.get(PREF_OUTPUT_PATH, "");
        lockOutput = prefs.getBoolean(PREF_LOCK_OUTPUT, false);

        view.getLockOutPutPath().setSelected(lockOutput);
        applyLockState();

        if (!lastInput.isBlank()) {
            Path inputPath = Paths.get(lastInput);
            if (Files.isDirectory(inputPath)) {
                userSelectedOutput = false;
                loadDirectory(inputPath);
            } else {
                view.getInputField().setText(lastInput);
            }
        }

        if (!lastOutput.isBlank()) {
            view.getOutputField().setText(lastOutput);
            if (Files.isDirectory(Paths.get(lastOutput))) {
                state.getOutputDirectory().set(Paths.get(lastOutput));
            }
        }
    }

    private void bindInputBrowse() {
        view.getInputBrowse().setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select workspace directory");
            String current = view.getInputField().getText();
            if (current != null && !current.isBlank()) {
                File currentFile = new File(current);
                if (currentFile.exists() && currentFile.isDirectory()) {
                    chooser.setInitialDirectory(currentFile);
                }
            }
            Stage stage = (Stage) view.getRoot().getScene().getWindow();
            File selected = chooser.showDialog(stage);
            if (selected != null) {
                userSelectedOutput = false;
                loadDirectory(selected.toPath());
            }
        });
    }

    private void bindOutputBrowse() {
        view.getOutputBrowse().setOnAction(e -> {
            if (lockOutput) {
                return;
            }
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select output directory");
            String current = view.getOutputField().getText();
            if (current != null && !current.isBlank()) {
                File currentFile = new File(current);
                if (currentFile.exists() && currentFile.isDirectory()) {
                    chooser.setInitialDirectory(currentFile);
                }
            }
            Stage stage = (Stage) view.getRoot().getScene().getWindow();
            File selected = chooser.showDialog(stage);
            if (selected != null) {
                userSelectedOutput = true;
                updateOutputPath(selected.toPath());
            }
        });
    }

    private void bindLockToggle() {
        view.getLockOutPutPath().setOnAction(e -> {
            lockOutput = view.getLockOutPutPath().isSelected();
            prefs.putBoolean(PREF_LOCK_OUTPUT, lockOutput);
            applyLockState();
        });
    }

    private void bindManualPathInput() {
        view.getInputField().focusedProperty().addListener((obs, oldFocused, newFocused) -> {
            if (!newFocused) {
                parseManualInput(view.getInputField().getText());
            }
        });
        view.getInputField().setOnAction(e -> parseManualInput(view.getInputField().getText()));

        view.getOutputField().setOnAction(e -> {
            if (!lockOutput) {
                Path path = resolveDirectory(view.getOutputField().getText());
                if (path != null) {
                    userSelectedOutput = true;
                    updateOutputPath(path);
                }
            }
        });
    }

    private void bindStateRescan() {
        state.getEngineType().addListener((obs, oldEngine, newEngine) -> rescanIfAvailable());
    }

    private void bindReloadButton() {
        if (view.getReloadButton() == null) {
            return;
        }
        view.getReloadButton().setOnAction(e -> {
            // 等价于“再次指定当前 input 路径”
            Path current = state.getInputDirectory().get();
            if (current != null && Files.isDirectory(current)) {
                loadDirectory(current);
            } else {
                appendLog("No valid input directory to reload.");
            }
        });
    }

    private void applyLockState() {
        view.getOutputField().setDisable(lockOutput);
        view.getOutputBrowse().setDisable(lockOutput);
    }

    private void parseManualInput(String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        Path path = resolveDirectory(text);
        if (path != null) {
            userSelectedOutput = false;
            loadDirectory(path);
        } else {
            appendLog("Input directory not found: " + text);
        }
    }

    private Path resolveDirectory(String text) {
        try {
            Path path = Paths.get(text.trim());
            if (Files.isDirectory(path)) {
                return path;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void updateOutputPath(Path path) {
        view.getOutputField().setText(path.toString());
        prefs.put(PREF_OUTPUT_PATH, path.toString());
        state.getOutputDirectory().set(path);
    }

    private void scanWorkspace(Path directory) {
        EngineType engineType = state.getEngineType().get();
        appendLog("Scanning " + directory + " (" + engineType.getDisplayName() + ")");
        try {
            List<WorkspaceCategory> categories = scanner.scan(directory, engineType);
            Platform.runLater(() -> {
                state.clearCategories();
                state.getCategories().addAll(categories);
            });
            appendLog("Scan completed: " + categories.size() + " categories found.");
        } catch (IOException ex) {
            appendLog("Scan failed: " + ex.getMessage());
        }
    }

    private void rescanIfAvailable() {
        Path current = state.getInputDirectory().get();
        if (current != null && Files.isDirectory(current)) {
            scanWorkspace(current);
        }
    }

    private void appendLog(String line) {
        view.getLogArea().appendText(line + System.lineSeparator());
    }
}

