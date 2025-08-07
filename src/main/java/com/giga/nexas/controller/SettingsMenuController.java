package com.giga.nexas.controller;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SettingsMenuController {

    private final MainViewController view;

    public void setup() {
        view.getMenuBar().getMenus().clear();

        Menu menu = new Menu("Settings");
        MenuItem item = new MenuItem("encoding / mode");
        item.setOnAction(e -> showSettingsDialog());

        menu.getItems().add(item);
        view.getMenuBar().getMenus().add(menu);
    }

    private void showSettingsDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Settings");
        dialog.setHeaderText("File encoding and mode");

        ComboBox<String> charsetBox = new ComboBox<>();
        charsetBox.getItems().addAll("UTF-8", "windows-31j(Japanese)");
        charsetBox.setValue("windows-31j");

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("BALDR SKY DIVEX", "BALDR HEART EXE");
        typeBox.setValue("BALDR SKY DIVEX");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("encoding:"), 0, 0);
        grid.add(charsetBox, 1, 0);
        grid.add(new Label("mode:"), 0, 1);
        grid.add(typeBox, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
}
