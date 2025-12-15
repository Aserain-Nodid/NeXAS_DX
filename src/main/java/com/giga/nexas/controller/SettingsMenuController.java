package com.giga.nexas.controller;

import com.giga.nexas.controller.model.EngineType;
import com.giga.nexas.controller.model.WorkspaceState;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 负责设置菜单对话框的控制器。
 */
@RequiredArgsConstructor
public class SettingsMenuController {

    private static final List<String> ENCODINGS = List.of("windows-31j", "UTF-8");

    private final MainViewController view;
    private final WorkspaceState state;

    public void setup() {
        view.getMenuBar().getMenus().clear();
        Menu settings = new Menu("Settings");
        MenuItem preferences = new MenuItem("Preferences");
        preferences.setOnAction(e -> showPreferencesDialog());
        settings.getItems().add(preferences);
        view.getMenuBar().getMenus().add(settings);
    }

    private void showPreferencesDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Preferences");
        dialog.setHeaderText("Adjust engine and encoding");

        ComboBox<EngineType> engineBox = new ComboBox<>();
        engineBox.getItems().addAll(EngineType.values());
        engineBox.getSelectionModel().select(state.getEngineType().get());

        ComboBox<String> charsetBox = new ComboBox<>();
        charsetBox.getItems().addAll(ENCODINGS);
        String currentCharset = state.getCharset().get();
        charsetBox.getSelectionModel().select(currentCharset != null ? currentCharset : state.getEngineType().get().getDefaultCharset());

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(16, 20, 10, 20));
        grid.add(new Label("Engine"), 0, 0);
        grid.add(engineBox, 1, 0);
        grid.add(new Label("Encoding"), 0, 1);
        grid.add(charsetBox, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                EngineType selectedEngine = engineBox.getValue();
                if (selectedEngine != null) {
                    state.getEngineType().set(selectedEngine);
                    view.getEngineSelector().getSelectionModel().select(selectedEngine);
                }
                String selectedCharset = charsetBox.getValue();
                if (selectedCharset != null && !selectedCharset.isBlank()) {
                    state.getCharset().set(selectedCharset);
                }
            }
        });
    }
}

